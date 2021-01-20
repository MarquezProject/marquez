package marquez.spark.agent.lifecycle;

import static marquez.spark.agent.lifecycle.RddExecutionContext.toZonedTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import marquez.spark.agent.MarquezContext;
import marquez.spark.agent.client.LineageEvent;
import marquez.spark.agent.client.LineageEvent.JobLink;
import marquez.spark.agent.client.LineageEvent.ParentRunFacet;
import marquez.spark.agent.client.LineageEvent.RunFacet;
import marquez.spark.agent.client.LineageEvent.RunLink;
import org.apache.spark.scheduler.ActiveJob;
import org.apache.spark.scheduler.JobFailed;
import org.apache.spark.scheduler.JobResult;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.QueryExecution;
import org.apache.spark.sql.execution.SQLExecution;
import org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionEnd;
import org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionStart;

@Slf4j
public class SparkSQLExecutionContext implements ExecutionContext {
  private final long executionId;

  private MarquezContext marquezContext;
  private UUID taskRunId;

  public SparkSQLExecutionContext(long executionId, MarquezContext marquezContext) {
    this.executionId = executionId;
    this.marquezContext = marquezContext;
    this.taskRunId = UUID.randomUUID();
  }

  public void start(SparkListenerSQLExecutionStart startEvent) {}

  public void end(SparkListenerSQLExecutionEnd endEvent) {}

  @Override
  public void setActiveJob(ActiveJob activeJob) {}

  @Override
  public void start(SparkListenerJobStart jobStart) {
    log.info("Starting job as part of spark-sql:" + jobStart.jobId());
    QueryExecution queryExecution = SQLExecution.getQueryExecution(executionId);
    if (queryExecution == null) {
      log.info("No execution info {}", queryExecution);
      return;
    }
    DatasetLogicalPlanTraverser.TraverserResult r =
        new DatasetLogicalPlanTraverser()
            .build(queryExecution.logical(), marquezContext.getJobNamespace());
    LineageEvent event =
        LineageEvent.builder()
            .inputs(r.getInputDataset())
            .outputs(r.getOutputDataset())
            .run(
                buildRun(
                    buildRunFacets(
                        buildLogicalPlanFacet(queryExecution.logical()), null, buildParentFacet())))
            .job(buildJob())
            .eventTime(toZonedTime(jobStart.time()))
            .eventType("START")
            .producer("spark")
            .build();

    marquezContext.emit(event);
  }

  private ParentRunFacet buildParentFacet() {
    return ParentRunFacet.builder()
        .job(
            JobLink.builder()
                .name(marquezContext.getJobName())
                .namespace(marquezContext.getJobNamespace())
                .build())
        .run(RunLink.builder().runId(marquezContext.getParentRunId()).build())
        .build();
  }

  @Override
  public void end(SparkListenerJobEnd jobEnd) {
    log.info("Ending job as part of spark-sql:" + jobEnd.jobId());
    QueryExecution queryExecution = SQLExecution.getQueryExecution(executionId);
    if (queryExecution == null) {
      log.info("No execution info {}", queryExecution);
      return;
    }
    DatasetLogicalPlanTraverser.TraverserResult r =
        new DatasetLogicalPlanTraverser()
            .build(queryExecution.logical(), marquezContext.getJobNamespace());
    LineageEvent event =
        LineageEvent.builder()
            .inputs(r.getInputDataset())
            .outputs(r.getOutputDataset())
            .run(
                buildRun(
                    buildRunFacets(
                        buildLogicalPlanFacet(queryExecution.logical()),
                        buildJobErrorFacet(jobEnd.jobResult()),
                        buildParentFacet())))
            .job(buildJob())
            .eventTime(toZonedTime(jobEnd.time()))
            .eventType(getEventType(jobEnd.jobResult()))
            .producer("org.apache.spark")
            .build();

    marquezContext.emit(event);
  }

  protected String getEventType(JobResult jobResult) {
    if (jobResult.getClass().getSimpleName().startsWith("JobSucceeded")) {
      return "COMPLETE";
    }
    return "FAIL";
  }

  private LineageEvent.Run buildRun(RunFacet facets) {
    return LineageEvent.Run.builder().runId(marquezContext.getParentRunId()).facets(facets).build();
  }

  protected RunFacet buildRunFacets(
      Object logicalPlanFacet, Object jobError, ParentRunFacet parentRunFacet) {
    Map<String, Object> additionalFacets = new HashMap<>();
    if (logicalPlanFacet != null) {
      additionalFacets.put("logicalPlan", logicalPlanFacet);
    }
    if (jobError != null) {
      additionalFacets.put("spark.exception", jobError);
    }
    return RunFacet.builder().parent(parentRunFacet).additional(additionalFacets).build();
  }

  private Map<String, Object> buildLogicalPlanFacet(LogicalPlan plan) {
    return new LogicalPlanFacetTraverser().visit(plan);
  }

  private Object buildJobErrorFacet(JobResult jobResult) {
    if (jobResult instanceof JobFailed) {
      return ((JobFailed) jobResult).exception().getMessage();
    }
    return null;
  }

  private LineageEvent.Job buildJob() {
    return LineageEvent.Job.builder()
        .namespace(marquezContext.getJobNamespace())
        .name(marquezContext.getJobName())
        .build();
  }
}
