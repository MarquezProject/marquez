package marquez.spark.agent;

import static marquez.client.models.JobType.BATCH;

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
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.QueryExecution;
import org.apache.spark.sql.execution.SQLExecution;
import org.apache.spark.sql.execution.SparkPlan;
import org.apache.spark.sql.execution.SparkPlanInfo;
import org.apache.spark.sql.execution.datasources.FileIndex;
import org.apache.spark.sql.execution.datasources.FilePartition;
import org.apache.spark.sql.execution.datasources.FileScanRDD;
import org.apache.spark.sql.execution.datasources.HadoopFsRelation;
import org.apache.spark.sql.execution.datasources.InsertIntoHadoopFsRelationCommand;
import org.apache.spark.sql.execution.datasources.LogicalRelation;
import org.apache.spark.sql.execution.datasources.PartitionedFile;
import org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionStart;
import org.apache.spark.sql.sources.BaseRelation;
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
  private static String runId;

  private static String get(String[] elements, String name, int index) {
    boolean check = elements.length > index + 1 && name.equals(elements[index]);
    if (check) {
      return elements[index + 1];
    } else {
      System.out.println("missing " + name + " in " + Arrays.toString(elements) + " at " + index);
      return "default";
    }
  }

  public static void init(String agentArgument) {
    System.out.println("*** Init SparkListener: " + agentArgument);
    String[] elements = agentArgument.split("\\/");
    String version = get(elements, "api", 1);
    if (version.equals("v1")) {
      System.out.println("marquez api v1");
    }
    jobNamespace = get(elements, "namespaces", 3);
    jobName = get(elements, "jobs", 5);
    runId = get(elements, "runs", 7);
    System.out.println(String.format("/api/%s/namespaces/%s/jobs/%s/runs/%s", version, jobNamespace, jobName, runId));
    marquezClient = Clients.newWriteOnlyClient(Backends.newLoggingBackend());
  }

  public static void registerActiveJob(ActiveJob activeJob) {
    System.out.println("*** Registered Active job:" + activeJob.jobId());
    System.out.println("***   - call site:  " + activeJob.callSite().shortForm());
    System.out.println("***   - rdd: " + activeJob.finalStage().rdd());
    new Exception("*** ActiveJob").printStackTrace(System.out);
    activeJobs.put(activeJob.jobId(), activeJob);
  }

  public static void registerOutput(PairRDDFunctions<?, ?> pairRDDFunctions, Configuration conf) {
    System.out.println("*** Registered output:" + pairRDDFunctions + " " + conf);
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

  public static void registerOutput(SparkSession session, String name, LogicalPlan command) {
    System.out.println("*** Registered output: (" + session + ", " + name +", " + command + ")");
    print("*** -> command: ", command);
    if (name.equals("save")) {
      InsertIntoHadoopFsRelationCommand c = ((InsertIntoHadoopFsRelationCommand)command);
      System.out.println("*** -> Path: " + c.outputPath());
    }
  }

  private static void print(String prefix, LogicalPlan plan) {
    String extra = "";
    if (plan instanceof LogicalRelation) {
      LogicalRelation lr = (LogicalRelation) plan;
      BaseRelation lrr = lr.relation();
      extra = "||| " + lrr + " " + lr.output() + " " + lr.catalogTable() + " " + lr.isStreaming();
      if (lrr instanceof HadoopFsRelation) {
        FileIndex location = ((HadoopFsRelation)lrr).location();
        extra += "|||" + Arrays.toString(location.inputFiles());
      }
    }
    System.out.println(prefix + plan.getClass().getSimpleName() + ":" + extra + " plan: " + plan);
    Collection<LogicalPlan> children = JavaConversions.asJavaCollection(plan.children());
    for (LogicalPlan child : children) {
      print(prefix + "  ", child);
    }
  }

  public static void registerOutput(QueryExecution qe) {
    System.out.println("*** Registered output QE: (" + qe + ")");
    print("*** -> physical: ", qe.executedPlan());
  }

  private static void print(String prefix, SparkPlan executedPlan) {
    System.out.println(prefix + executedPlan.treeString(true, true));
  }

  private static void sparkSQLExecStart(
      SparkListenerSQLExecutionStart sparkListenerSQLExecutionStart) {
    SparkPlanInfo sparkPlanInfo = sparkListenerSQLExecutionStart.sparkPlanInfo();
    QueryExecution qe = SQLExecution.getQueryExecution(sparkListenerSQLExecutionStart.executionId());
    print("%*** -> logical: ", qe.logical());
    print("%*** -> physical: ", qe.executedPlan());
    print("sparkPlanInfo Exec:"+ sparkListenerSQLExecutionStart.executionId()+ "  ", sparkPlanInfo);
  }

  private static void print(String prefix, SparkPlanInfo i) {
    System.out.println(prefix + i.nodeName()+ "(" + i.simpleString()+ " metadata: " + i.metadata() + " metrics: "+ i.metrics() + ")" );
    Collection<SparkPlanInfo> children = JavaConversions.asJavaCollection(i.children());
    for (SparkPlanInfo child : children) {
      print(prefix + "  ", child);
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

    if (jobEnd.jobResult() instanceof JobFailed) {
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
    } else if (rdd instanceof FileScanRDD) {
      Collection<FilePartition> filePartitions = JavaConversions.asJavaCollection(((FileScanRDD)rdd).filePartitions());
      List<Path> paths = new ArrayList<Path>();
      for (FilePartition filePartition : filePartitions) {
        PartitionedFile[] files = filePartition.files();
        for (PartitionedFile file : files) {
          paths.add(new Path(file.filePath()));
        }
      }
      inputPaths = paths.toArray(new Path[paths.size()]);
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
    System.out.println("*** instrument:" + context);
    Class<?>[] interfaces = {SparkListenerInterface.class};
    SparkListenerInterface listener = (SparkListenerInterface)Proxy.newProxyInstance(SparkListener.class.getClassLoader(), interfaces, new InvocationHandler(){

      int counter = 0;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("onExecutorMetricsUpdate")
            || method.getName().equals("onBlockUpdated")
            || method.getName().equals("onTaskStart")
            || method.getName().equals("onTaskEnd")
            || method.getName().equals("onStageSubmitted")
            || method.getName().equals("onStageCompleted")
            ) {
          return null;
        }
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
          } else if (method.getName().equals("onOtherEvent") && args[0] instanceof SparkListenerSQLExecutionStart) {
            sparkSQLExecStart((SparkListenerSQLExecutionStart)args[0]);
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
