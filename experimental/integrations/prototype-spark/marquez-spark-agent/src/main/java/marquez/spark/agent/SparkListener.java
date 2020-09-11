package marquez.spark.agent;

import static marquez.client.models.JobType.BATCH;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.Dependency;
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.HadoopRDD;
import org.apache.spark.rdd.NewHadoopRDD;
import org.apache.spark.rdd.PairRDDFunctions;
import org.apache.spark.rdd.RDD;
import org.apache.spark.scheduler.ActiveJob;
import org.apache.spark.scheduler.JobFailed;
import org.apache.spark.scheduler.ResultStage;
import org.apache.spark.scheduler.SparkListenerInterface;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.Stage;
import org.apache.spark.scheduler.StageInfo;
import org.apache.spark.storage.RDDInfo;

import marquez.client.Backends;
import marquez.client.Clients;
import marquez.client.MarquezWriteOnlyClient;
import marquez.client.models.DatasetId;
import marquez.client.models.DatasetMeta;
import marquez.client.models.DbTableMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.RunMeta;
import scala.collection.JavaConversions;


public class SparkListener {

  private static Map<Integer, ActiveJob> activeJobs = Collections.synchronizedMap(new HashMap<>());

  private static Map<RDD<?>, Configuration> outputs = Collections.synchronizedMap(new HashMap<>());

  private static Map<Integer, UUID> jobIdToRunId = Collections.synchronizedMap(new HashMap<>());

  private static MarquezWriteOnlyClient marquezClient;

  private static String jobNamespace;
  private static String jobName;

  public static void init(String agentArgument) {
    System.out.println("Init SparkListener: " + agentArgument);
    marquezClient = Clients.newWriteOnlyClient(Backends.newFileBackend(new File("/tmp/marquez.request.log")));
    jobNamespace = "jobs";
    jobName = "Spark";
  }

  public static void registerActiveJob(ActiveJob activeJob) {
    System.out.println("Registered Active job:" + activeJob.jobId());
    activeJobs.put(activeJob.jobId(), activeJob);
  }

  public static void registerOutput(PairRDDFunctions<?, ?> pairRDDFunctions, Configuration conf) {
    Field[] declaredFields = pairRDDFunctions.getClass().getDeclaredFields();
    for (Field field : declaredFields) {
      if (field.getName().endsWith("self") && RDD.class.isAssignableFrom(field.getType())) {
        field.setAccessible(true);
        try {
          RDD<?> rdd = (RDD<?>)field.get(pairRDDFunctions);
          outputs.put(rdd, conf);
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace(System.out);
        }
      }
    }
  }

  private static void jobStarted(SparkListenerJobStart jobStart) {
    ActiveJob activeJob = activeJobs.get(jobStart.jobId());
    RDD<?> finalRDD = activeJob.finalStage().rdd();
    Set<RDD<?>> rdds = flattenRDDs(finalRDD);
    List<String> inputs = findInputs(rdds);
    List<String> outputs = findOutputs(rdds);
    System.out.println("++++++++++++ Job " + jobStart.jobId() + " started at: " + jobStart.time() + " Inputs: " + inputs + " Outputs: " + outputs);
    Set<DatasetId> inputIds = new HashSet<DatasetId>();
    for (String input : inputs) {
      String protocol;
      try {
        URL inputUrl = new URL(input);
        protocol=inputUrl.getProtocol();
      } catch (MalformedURLException e) {
        protocol = "unknown";
      }
      String namespace = protocol;
      String name = input;
      DatasetMeta meta = DbTableMeta.builder().sourceName(protocol).physicalName(name).build();
      marquezClient.createDataset(namespace, name, meta);
      inputIds.add(new DatasetId(namespace, name));
    }
    Set<DatasetId> outputIds = new HashSet<DatasetId>();
    for (String output : outputs) {
      String protocol;
      try {
        URL inputUrl = new URL(output);
        protocol=inputUrl.getProtocol();
      } catch (MalformedURLException e) {
        protocol = "unknown";
      }
      String namespace = protocol;
      String name = output;
      DatasetMeta meta = DbTableMeta.builder().sourceName(protocol).physicalName(name).build();
//      marquezClient.createDataset(namespace, name, meta);
      outputIds.add(new DatasetId(namespace, name));
    }
    marquezClient.createJob(jobNamespace, jobName, JobMeta.builder().inputs(inputIds).outputs(outputIds).type(BATCH).build());
    UUID runId = UUID.randomUUID();
    jobIdToRunId.put(jobStart.jobId(), runId);
    marquezClient.createRun(jobNamespace, jobName, new RunMeta(runId.toString(), null, null, null));
    marquezClient.markRunAsRunning(runId.toString(), Instant.ofEpochMilli(jobStart.time()));
  }

  private static void jobEnded(SparkListenerJobEnd jobEnd) {
    ActiveJob activeJob = activeJobs.get(jobEnd.jobId());
    RDD<?> finalRDD = activeJob.finalStage().rdd();
    Set<RDD<?>> rdds = flattenRDDs(finalRDD);
    List<String> outputs = findOutputs(rdds);
    UUID runId = jobIdToRunId.get(jobEnd.jobId());
    for (String output : outputs) {
      String protocol;
      try {
        URL inputUrl = new URL(output);
        protocol=inputUrl.getProtocol();
      } catch (MalformedURLException e) {
        protocol = "unknown";
      }
      String namespace = protocol;
      String name = output;
      DatasetMeta meta = DbTableMeta.builder().sourceName(protocol).physicalName(name).runId(runId.toString()).build();
      marquezClient.createDataset(namespace, name, meta);
    }
    Instant at = Instant.ofEpochMilli(jobEnd.time());

    if (jobEnd.jobResult() instanceof JobFailed){
      Exception e = ((JobFailed)jobEnd.jobResult()).exception();
      e.printStackTrace(System.out);
      marquezClient.markRunAsFailed(runId.toString(), at);
    } else if (jobEnd.jobResult().getClass().getSimpleName().startsWith("JobSucceeded")){
      System.out.println(jobEnd.jobResult().getClass().getName() );
      marquezClient.markRunAsCompleted(runId.toString(), at);
    } else {
      System.out.println("Unknown status: " + jobEnd.jobResult());
    }
    System.out.println("++++++++++++ Job " + jobEnd.jobId() + " ended with status " + jobEnd.jobResult() + " at: " + jobEnd.time());
  }

  private static List<String> findOutputs(Set<RDD<?>> rdds) {
    List<String> result = new ArrayList<>();
    for (RDD<?> rdd: rdds) {
      Path outputPath = getOutputPath(rdd);
      if (outputPath != null) {
        result.add(outputPath.toUri().toString());
      }
    }
    return result;
  }


  private static Set<RDD<?>> flattenRDDs(RDD<?> rdd) {
    Set<RDD<?>> rdds = new HashSet<>();
    rdds.add(rdd);
    Collection<Dependency<?>> deps = JavaConversions.asJavaCollection(rdd.dependencies());
    for (Dependency<?> dep : deps) {
      rdds.addAll(flattenRDDs(dep.rdd()));
    }
    return rdds;
  }

  private static List<String> findInputs(Set<RDD<?>> rdds) {
    List<String> result = new ArrayList<>();
    for (RDD<?> rdd: rdds) {
      Path[] inputPaths = getInputPaths(rdd);
      if (inputPaths != null) {
        for (Path path : inputPaths) {
          result.add(path.toUri().toString());
        }
      }
    }
    return result;
  }

  private static Path[] getInputPaths(RDD<?> rdd) {
    Path[] inputPaths = null;
    if (rdd instanceof HadoopRDD) {
      inputPaths = org.apache.hadoop.mapred.FileInputFormat.getInputPaths(((HadoopRDD<?,?>)rdd).getJobConf());
    } else if (rdd instanceof NewHadoopRDD) {
      try {
        inputPaths = org.apache.hadoop.mapreduce.lib.input.FileInputFormat.getInputPaths(new Job(((NewHadoopRDD<?,?>)rdd).getConf()));
      } catch (IOException e) {
        e.printStackTrace(System.out);
      }
    }
    return inputPaths;
  }

  private static void printRDDs(String prefix, RDD<?> rdd) {

    Path outputPath = getOutputPath(rdd);
    Path[] inputPath = getInputPaths(rdd);
    System.out.println(prefix + rdd + (outputPath == null ? "" : " output: " + outputPath) + (inputPath == null ? "" : " input: " + Arrays.toString(inputPath)));
    Collection<Dependency<?>> deps = JavaConversions.asJavaCollection(rdd.dependencies());
    for (Dependency<?> dep : deps) {
      printRDDs(prefix + " \\ ", dep.rdd());
    }
  }

  private static void printStages(String prefix, Stage stage) {
    if (stage instanceof ResultStage) {
      ResultStage resultStage = (ResultStage)stage;
      System.out.println(prefix +"(stageId:" + stage.id() + ") Result:" + resultStage.func());
    }
    printRDDs(prefix +"(stageId:" + stage.id() + ")-("+stage.getClass().getSimpleName()+")- RDD: ",  stage.rdd());
    Collection<Stage> parents = JavaConversions.asJavaCollection(stage.parents());
    for (Stage parent : parents) {
      printStages(prefix + " \\ ", parent);
    }
  }

  private static Path getOutputPath(RDD<?> rdd) {
    Configuration conf = outputs.get(rdd);
    if (conf == null) {
      return null;
    }
    // "new" mapred api
    Path path = org.apache.hadoop.mapred.FileOutputFormat.getOutputPath(new JobConf(conf));
    if (path == null) {
      try {
        // old fashioned mapreduce api
        path =  org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getOutputPath(new Job(conf));
      } catch (IOException exception) {
        exception.printStackTrace(System.out);
      }
    }
    return path;
  }

  public static void instrument(SparkContext context) {
    Class<?>[] interfaces = {SparkListenerInterface.class};
    SparkListenerInterface listener = (SparkListenerInterface)Proxy.newProxyInstance(SparkListener.class.getClassLoader(), interfaces, new InvocationHandler(){

      int counter = 0;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int call = counter ++;
        String prefix = "MQZ - " + call;
        String eventType = "";
        if (args.length == 1 & args[0]!=null) {
          eventType = "(" + args[0].getClass().getSimpleName() + ")";
        }
        System.out.println(  prefix + "-(START)----- MARQUEZ -: " + method.getName() + eventType);
        System.out.println(  prefix + "-(method)----" + method);
        for (Object arg : args) {
          System.out.println(prefix + "-(arg)-------  " + arg);
        }
//        new Exception("MQZ - " + call + "-(Called from)-").printStackTrace(System.out);
        if (args.length == 1 & args[0]!=null) {
          if (method.getName().equals("onJobStart") && args[0] instanceof SparkListenerJobStart) {
            SparkListenerJobStart jobStart = (SparkListenerJobStart)args[0];

            jobStarted(jobStart);
            int jobId = jobStart.jobId();
            String jp = prefix + "-(job:"+jobId+")-";
            ActiveJob activeJob = activeJobs.get(jobId);
            System.out.println(jp + activeJob + jobStart.properties().toString());
            Stage finalStage = activeJob.finalStage();
            printStages(jp + "-(stage)-", finalStage);
            List<StageInfo> stageInfos = JavaConversions.seqAsJavaList(jobStart.stageInfos());
            for (StageInfo stageInfo : stageInfos) {
              String sp = jp + "-(stageInfo)-(stageId:"+stageInfo.stageId()+")----" + stageInfo.name();
              System.out.println(sp + "-parents:" + stageInfo.parentIds());
              List<RDDInfo> rddInfos = JavaConversions.seqAsJavaList(stageInfo.rddInfos());
              for (RDDInfo rddInfo : rddInfos) {
                System.out.println( sp + "\\-(rddInfo)-" + rddInfo);
              }
            }
          } else if (method.getName().equals("onJobEnd") && args[0] instanceof SparkListenerJobEnd) {
            SparkListenerJobEnd jobEnd = (SparkListenerJobEnd)args[0];
            int jobId = jobEnd.jobId();
            String jp = prefix + "-(job:"+jobId+")-";
            ActiveJob activeJob = activeJobs.get(jobId);
            System.out.println(jp + activeJob + jobEnd);
            jobEnded(jobEnd);
          }
        }

        System.out.println( prefix + "-(END)------- MARQUEZ -: " + method.getName() + eventType);
        return null;
      }

    });
    context.addSparkListener(listener);
  }

  public static void close() {
    try {
      marquezClient.close();
      marquezClient = null;
    } catch (IOException e) {
      e.printStackTrace(System.out);
    }
  }
}
