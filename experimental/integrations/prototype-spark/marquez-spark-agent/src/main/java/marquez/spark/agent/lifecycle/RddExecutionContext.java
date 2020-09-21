package marquez.spark.agent.lifecycle;

import static marquez.spark.agent.lifecycle.Rdds.flattenRDDs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.Dependency;
import org.apache.spark.rdd.HadoopRDD;
import org.apache.spark.rdd.NewHadoopRDD;
import org.apache.spark.rdd.RDD;
import org.apache.spark.scheduler.ActiveJob;
import org.apache.spark.scheduler.JobFailed;
import org.apache.spark.scheduler.ResultStage;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import marquez.spark.agent.MarquezContext;
import marquez.spark.agent.SparkListener;
import scala.collection.JavaConversions;

public class RddExecutionContext implements ExecutionContext {
  private static final Logger logger = LoggerFactory.getLogger(RddExecutionContext.class);

  private final int jobId;
  private final MarquezContext marquezContext;
  private List<String> inputs;
  private List<String> outputs;
  private StringBuilder extraInfo = new StringBuilder();
  private UUID runId;

  public RddExecutionContext(int jobId, MarquezContext marquezContext) {
    this.jobId = jobId;
    this.marquezContext = marquezContext;
  }

  @Override
  public void setActiveJob(ActiveJob activeJob) {
    RDD<?> finalRDD = activeJob.finalStage().rdd();
    extraInfo.append("Registered Active job, rdd: ").append(finalRDD).append("\n");
    Set<RDD<?>> rdds = flattenRDDs(finalRDD);
    this.inputs = findInputs(rdds);
    this.outputs = findOutputs(rdds);
    Stage finalStage = activeJob.finalStage();
    printStages("  -(stage)-", finalStage);
  }

  @Override
  public void start(SparkListenerJobStart jobStart) {
    extraInfo.append(Rdds.toString(jobStart));
    this.runId = marquezContext.startRun(jobStart.time(), inputs, outputs, extraInfo.toString());
  }

  @Override
  public void end(SparkListenerJobEnd jobEnd) {
    extraInfo.append("end:\n");
    if (jobEnd.jobResult() instanceof JobFailed) {
      Exception e = ((JobFailed)jobEnd.jobResult()).exception();
      e.printStackTrace(System.out);
      marquezContext.failure(runId, jobId, jobEnd.time(), outputs, e);
    } else if (jobEnd.jobResult().getClass().getSimpleName().startsWith("JobSucceeded")){
      marquezContext.success(runId, jobId, jobEnd.time(), outputs);
    } else {
      extraInfo.append("Unknown status: " ).append(jobEnd.jobResult()).append("\n");
    }
    logger.info(jobId + " ended\n" + extraInfo.toString());
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

  private void printRDDs(String prefix, RDD<?> rdd) {
    extraInfo.append(prefix).append(rdd).append("\n");
    Collection<Dependency<?>> deps = JavaConversions.asJavaCollection(rdd.dependencies());
    for (Dependency<?> dep : deps) {
      printRDDs(prefix + "  ", dep.rdd());
    }
  }

  private void printStages(String prefix, Stage stage) {
    if (stage instanceof ResultStage) {
      ResultStage resultStage = (ResultStage)stage;
      extraInfo.append(prefix).append("stage: ").append(stage.id()).append(" Result: ").append(resultStage.func()).append("\n");
    }
    printRDDs(prefix + "(stageId:" + stage.id() + ")-("+stage.getClass().getSimpleName()+")- RDD: ",  stage.rdd());
    Collection<Stage> parents = JavaConversions.asJavaCollection(stage.parents());
    for (Stage parent : parents) {
      printStages(prefix + " \\ ", parent);
    }
  }

  private static Path getOutputPath(RDD<?> rdd) {
    Configuration conf = SparkListener.removeConfigForRDD(rdd);
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

}
