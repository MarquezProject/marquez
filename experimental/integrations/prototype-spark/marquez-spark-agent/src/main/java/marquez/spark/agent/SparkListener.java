package marquez.spark.agent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.apache.spark.scheduler.ResultStage;
import org.apache.spark.scheduler.SparkListenerInterface;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.Stage;
import org.apache.spark.scheduler.StageInfo;
import org.apache.spark.storage.RDDInfo;

import scala.collection.JavaConversions;


public class SparkListener {

  private static Map<Integer, ActiveJob> activeJobs = Collections.synchronizedMap(new HashMap<>());

  private static Map<RDD, Configuration> outputs = Collections.synchronizedMap(new HashMap<>());

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

  private static Path getOutputPath(RDD rdd) {
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

      private void jobStarted(SparkListenerJobStart jobStart) {
        ActiveJob activeJob = activeJobs.get(jobStart.jobId());
        RDD<?> finalRDD = activeJob.finalStage().rdd();
        Set<RDD<?>> rdds = flattenRDDs(finalRDD);
        List<String> inputs = findInputs(rdds);
        List<String> outputs = findOutputs(rdds);
        System.out.println("++++++++++++ Job " + jobStart.jobId() + " started at: " + jobStart.time() + " Inputs: " + inputs + " Outputs: " + outputs);
      }

      private void jobEnded(SparkListenerJobEnd jobEnd) {
        System.out.println("++++++++++++ Job " + jobEnd.jobId() + " ended with status " + jobEnd.jobResult() + " at: " + jobEnd.time());
      }

      private List<String> findOutputs(Set<RDD<?>> rdds) {
        List<String> result = new ArrayList<>();
        for (RDD<?> rdd: rdds) {
          Path outputPath = getOutputPath(rdd);
          if (outputPath != null) {
            result.add(outputPath.toUri().toString());
          }
        }
        return result;
      }


      private Set<RDD<?>> flattenRDDs(RDD<?> rdd) {
        Set<RDD<?>> rdds = new HashSet<>();
        rdds.add(rdd);
        Collection<Dependency<?>> deps = JavaConversions.asJavaCollection(rdd.dependencies());
        for (Dependency<?> dep : deps) {
          rdds.addAll(flattenRDDs(dep.rdd()));
        }
        return rdds;
      }

      private List<String> findInputs(Set<RDD<?>> rdds) {
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

      private Path[] getInputPaths(RDD<?> rdd) {
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

      private void printRDDs(String prefix, RDD<?> rdd) {

        Path outputPath = getOutputPath(rdd);
        Path[] inputPath = getInputPaths(rdd);
        System.out.println(prefix + rdd + (outputPath == null ? "" : " output: " + outputPath) + (inputPath == null ? "" : " input: " + Arrays.toString(inputPath)));
        Collection<Dependency<?>> deps = JavaConversions.asJavaCollection(rdd.dependencies());
        for (Dependency<?> dep : deps) {
          printRDDs(prefix + " \\ ", dep.rdd());
        }
      }

      private void printStages(String prefix, Stage stage) {
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
    });
    context.addSparkListener(listener);
  }
}
