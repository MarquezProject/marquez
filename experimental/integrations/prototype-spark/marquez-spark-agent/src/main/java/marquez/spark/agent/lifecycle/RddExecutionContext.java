package marquez.spark.agent.lifecycle;

import static marquez.spark.agent.lifecycle.Rdds.flattenRDDs;
import static scala.collection.JavaConversions.asJavaCollection;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import marquez.spark.agent.MarquezContext;
import marquez.spark.agent.SparkListener;
import marquez.spark.agent.client.LineageEvent;
import marquez.spark.agent.client.LineageEvent.Dataset;
import marquez.spark.agent.client.LineageEvent.Run;
import marquez.spark.agent.client.LineageEvent.RunFacet;
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
import org.apache.spark.scheduler.JobResult;
import org.apache.spark.scheduler.ResultStage;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.Stage;

@Slf4j
public class RddExecutionContext implements ExecutionContext {
  private final MarquezContext marquezContext;
  private List<String> inputs;
  private List<String> outputs;

  public RddExecutionContext(int jobId, MarquezContext marquezContext) {
    this.marquezContext = marquezContext;
  }

  @Override
  public void setActiveJob(ActiveJob activeJob) {
    RDD<?> finalRDD = activeJob.finalStage().rdd();
    Set<RDD<?>> rdds = flattenRDDs(finalRDD);
    this.inputs = findInputs(rdds);
    this.outputs = findOutputs(rdds);
  }

  @Override
  public void start(SparkListenerJobStart jobStart) {
    asJavaCollection(jobStart.stageInfos());

    LineageEvent event =
        LineageEvent.builder()
            .inputs(buildInputs(inputs))
            .outputs(buildOutputs(outputs))
            .run(buildRun(buildRunFacets(null)))
            .job(buildJob())
            .eventTime(toZonedTime(jobStart.time()))
            .eventType("START")
            .producer("org.apache.spark")
            .build();

    marquezContext.emit(event);
  }

  @Override
  public void end(SparkListenerJobEnd jobEnd) {
    LineageEvent event =
        LineageEvent.builder()
            .inputs(buildInputs(inputs))
            .outputs(buildOutputs(outputs))
            .run(buildRun(buildRunFacets(jobEnd.jobResult())))
            .job(buildJob())
            .eventTime(toZonedTime(jobEnd.time()))
            .eventType(getEventType(jobEnd.jobResult()))
            .producer("org.apache.spark")
            .build();

    marquezContext.emit(event);
  }

  public static ZonedDateTime toZonedTime(long time) {
    Instant i = Instant.ofEpochMilli(time);
    return ZonedDateTime.ofInstant(i, ZoneOffset.UTC);
  }

  private RunFacet buildRunFacets(JobResult jobResult) {
    if (jobResult instanceof JobFailed) {
      Exception e = ((JobFailed) jobResult).exception();
      return RunFacet.builder()
          .additional(ImmutableMap.of("spark.exception", e.getMessage()))
          .build();
    }
    return null;
  }

  private LineageEvent.Job buildJob() {
    return LineageEvent.Job.builder()
        .namespace(marquezContext.getJobNamespace())
        .name(marquezContext.getJobName())
        .build();
  }

  private LineageEvent.Run buildRun(RunFacet facets) {
    return Run.builder().runId(marquezContext.getParentRunId()).facets(facets).build();
  }

  private List<Dataset> buildOutputs(List<String> outputs) {
    return outputs.stream()
        .map(
            name ->
                Dataset.builder().name(name).namespace(marquezContext.getJobNamespace()).build())
        .collect(Collectors.toList());
  }

  private List<Dataset> buildInputs(List<String> inputs) {
    return inputs.stream()
        .map(
            name ->
                Dataset.builder().name(name).namespace(marquezContext.getJobNamespace()).build())
        .collect(Collectors.toList());
  }

  private static List<String> findOutputs(Set<RDD<?>> rdds) {
    List<String> result = new ArrayList<>();
    for (RDD<?> rdd : rdds) {
      Path outputPath = getOutputPath(rdd);
      if (outputPath != null) {
        result.add(outputPath.toUri().toString());
      }
    }
    return result;
  }

  private static List<String> findInputs(Set<RDD<?>> rdds) {
    List<String> result = new ArrayList<>();
    for (RDD<?> rdd : rdds) {
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
      inputPaths =
          org.apache.hadoop.mapred.FileInputFormat.getInputPaths(
              ((HadoopRDD<?, ?>) rdd).getJobConf());
    } else if (rdd instanceof NewHadoopRDD) {
      try {
        inputPaths =
            org.apache.hadoop.mapreduce.lib.input.FileInputFormat.getInputPaths(
                new Job(((NewHadoopRDD<?, ?>) rdd).getConf()));
      } catch (IOException e) {
        e.printStackTrace(System.out);
      }
    }
    return inputPaths;
  }

  private void printRDDs(String prefix, RDD<?> rdd) {
    Collection<Dependency<?>> deps = asJavaCollection(rdd.dependencies());
    for (Dependency<?> dep : deps) {
      printRDDs(prefix + "  ", dep.rdd());
    }
  }

  private void printStages(String prefix, Stage stage) {
    if (stage instanceof ResultStage) {
      ResultStage resultStage = (ResultStage) stage;
    }
    printRDDs(
        prefix + "(stageId:" + stage.id() + ")-(" + stage.getClass().getSimpleName() + ")- RDD: ",
        stage.rdd());
    Collection<Stage> parents = asJavaCollection(stage.parents());
    for (Stage parent : parents) {
      printStages(prefix + " \\ ", parent);
    }
  }

  private static Path getOutputPath(RDD<?> rdd) {
    Configuration conf = SparkListener.getConfigForRDD(rdd);
    if (conf == null) {
      return null;
    }
    // "new" mapred api
    Path path = org.apache.hadoop.mapred.FileOutputFormat.getOutputPath(new JobConf(conf));
    if (path == null) {
      try {
        // old fashioned mapreduce api
        path = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getOutputPath(new Job(conf));
      } catch (IOException exception) {
        exception.printStackTrace(System.out);
      }
    }
    return path;
  }

  protected String getEventType(JobResult jobResult) {
    if (jobResult.getClass().getSimpleName().startsWith("JobSucceeded")) {
      return "COMPLETE";
    }
    return "FAIL";
  }
}
