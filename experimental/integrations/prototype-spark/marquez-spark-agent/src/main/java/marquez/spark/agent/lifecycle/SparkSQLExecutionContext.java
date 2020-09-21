package marquez.spark.agent.lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.spark.rdd.RDD;
import org.apache.spark.scheduler.ActiveJob;
import org.apache.spark.scheduler.JobFailed;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.QueryExecution;
import org.apache.spark.sql.execution.SQLExecution;
import org.apache.spark.sql.execution.SparkPlan;
import org.apache.spark.sql.execution.SparkPlanInfo;
import org.apache.spark.sql.execution.datasources.FileIndex;
import org.apache.spark.sql.execution.datasources.HadoopFsRelation;
import org.apache.spark.sql.execution.datasources.InsertIntoHadoopFsRelationCommand;
import org.apache.spark.sql.execution.datasources.LogicalRelation;
import org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionEnd;
import org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionStart;
import org.apache.spark.sql.sources.BaseRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import marquez.spark.agent.MarquezContext;
import marquez.spark.agent.SparkListener;
import scala.collection.JavaConversions;

public class SparkSQLExecutionContext implements ExecutionContext {
  private static final Logger logger = LoggerFactory.getLogger(SparkSQLExecutionContext.class);

  private final long executionId;
  private List<String> inputs;
  private List<String> outputs;
  private StringBuilder extraInfo = new StringBuilder();
  private MarquezContext marquezContext;
  private Boolean success = null;

  private Exception error;

  private UUID runId;

  private int jobId;

  public SparkSQLExecutionContext(long executionId, MarquezContext marquezContext) {
    this.executionId = executionId;
    this.marquezContext = marquezContext;
  }

  public void start(SparkListenerSQLExecutionStart startEvent) {
    logger.info("Spark sql execution started " + startEvent);
    SparkPlanInfo sparkPlanInfo = startEvent.sparkPlanInfo();
    QueryExecution queryExecution = SQLExecution.getQueryExecution(executionId);
    this.inputs = findInputs(queryExecution.logical());
    this.outputs = findOutputs(queryExecution.logical());
    print("%*** -> logical: ", queryExecution.logical());
    print("%*** -> physical: ", queryExecution.executedPlan());
    print("sparkPlanInfo Exec:"+ startEvent.executionId()+ "  ", sparkPlanInfo);
    this.runId = marquezContext.startRun(startEvent.time(), inputs, outputs, extraInfo.toString());
  }

  private List<String> findInputs(LogicalPlan logical) {
    List<String> result = new ArrayList<String>();
    Collection<LogicalPlan> leaves = JavaConversions.asJavaCollection(logical.collectLeaves());
    for (LogicalPlan leaf : leaves) {
        if (leaf instanceof LogicalRelation) {
          LogicalRelation lr = (LogicalRelation) leaf;
          BaseRelation lrr = lr.relation();
          if (lrr instanceof HadoopFsRelation) {
            FileIndex location = ((HadoopFsRelation)lrr).location();
            Collection<Path> rootPaths = JavaConversions.asJavaCollection(location.rootPaths());
            for (Path rootPath : rootPaths) {
              result.add(rootPath.toUri().toString());
            }
          } else {
            extraInfo.append("unknown baseRelation in leaf: ").append(leaf.getClass().getName())
            .append("\n  ").append(leaf)
            .append("\n  ").append(lrr.getClass().getName()).append(" ").append(lrr).append("\n");
          }
        }else {
          extraInfo.append("unknown leaf node: ").append(leaf.getClass().getName())
          .append("\n  ").append(leaf).append("\n");
        }
    }
    return result;
  }

  private List<String> findOutputs(LogicalPlan logical) {
    if (logical instanceof InsertIntoHadoopFsRelationCommand) {
      InsertIntoHadoopFsRelationCommand insert = (InsertIntoHadoopFsRelationCommand)logical;
      return ImmutableList.of(insert.outputPath().toUri().toString());
    } else {
      extraInfo.append("No Output found for plan: ").append(logical.getClass().getName()).append(" ").append(logical).append("\n");
      return ImmutableList.of();
    }
  }

  private void print(String prefix, SparkPlanInfo i) {
    extraInfo.append(prefix + i.nodeName()+ "(" + i.simpleString()+ " metadata: " + i.metadata() + " metrics: "+ i.metrics() + ")" ).append("\n");
    Collection<SparkPlanInfo> children = JavaConversions.asJavaCollection(i.children());
    for (SparkPlanInfo child : children) {
      print(prefix + "  ", child);
    }
  }

  private void print(String prefix, SparkPlan executedPlan) {
    extraInfo.append(prefix + executedPlan.treeString(true, true)).append("\n");
  }

  private void print(String prefix, LogicalPlan plan) {
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
    extraInfo.append(prefix + plan.getClass().getSimpleName() + ":" + extra + " plan: " + plan).append("\n");
    Collection<LogicalPlan> children = JavaConversions.asJavaCollection(plan.children());
    for (LogicalPlan child : children) {
      print(prefix + "  ", child);
    }
  }

  public void end(SparkListenerSQLExecutionEnd endEvent) {
    logger.info("Spark sql execution ended " + endEvent);
    if (success == null) {
      extraInfo.append("unknown end status\n");
    } else if (success) {
      marquezContext.success(runId, jobId, endEvent.time(), outputs);
    } else  {
      marquezContext.failure(runId, jobId, endEvent.time(), outputs, error );
    }
    logger.info("job ended: " + endEvent + " \n" + extraInfo.toString());
  }

  @Override
  public void setActiveJob(ActiveJob activeJob) {
    jobId = activeJob.jobId();
    RDD<?> finalRdd = activeJob.finalStage().rdd();
    logger.info("Registered Active job as part of spark-sql:" + activeJob.jobId() + " rdd: " + finalRdd);
    Set<RDD<?>> rdds = Rdds.flattenRDDs(finalRdd);
    for (RDD<?> rdd : rdds) {
      Configuration config = SparkListener.removeConfigForRDD(rdd);
      if (config != null) {
        extraInfo.append("unexpected rdd => conf mapping\n  ").append(rdd).append(" => ").append(config).append("\n");
        logger.warn("This should not be here: " + rdd + " => " + config);
      }
    }
  }

  @Override
  public void start(SparkListenerJobStart jobStart) {
    logger.info("Starting job as part of spark-sql:" + jobStart.jobId());
    extraInfo.append(Rdds.toString(jobStart));
  }

  @Override
  public void end(SparkListenerJobEnd jobEnd) {
    logger.info("Ending job as part of spark-sql:" + jobEnd.jobId());
    extraInfo.append("end: ").append(jobEnd).append("\n");
    if (jobEnd.jobResult() instanceof JobFailed) {
      success = false;
      error = ((JobFailed)jobEnd.jobResult()).exception();
    } else if (jobEnd.jobResult().getClass().getSimpleName().startsWith("JobSucceeded")){
      success = true;
    } else {
      extraInfo.append("Unknown status: " ).append(jobEnd.jobResult()).append("\n");
    }
  }
}

