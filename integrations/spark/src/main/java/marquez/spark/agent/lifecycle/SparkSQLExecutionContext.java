package marquez.spark.agent.lifecycle;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import marquez.spark.agent.MarquezContext;
import marquez.spark.agent.client.LineageEvent;
import marquez.spark.agent.client.LineageEvent.Dataset;
import marquez.spark.agent.client.LineageEvent.JobLink;
import marquez.spark.agent.client.LineageEvent.ParentRunFacet;
import marquez.spark.agent.client.LineageEvent.RunFacet;
import marquez.spark.agent.client.LineageEvent.RunLink;
import marquez.spark.agent.client.OpenLineageClient;
import marquez.spark.agent.facets.ErrorFacet;
import marquez.spark.agent.facets.LogicalPlanFacet;
import marquez.spark.agent.lifecycle.plan.PlanUtils;
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
import scala.PartialFunction;
import scala.collection.JavaConversions;

@Slf4j
public class SparkSQLExecutionContext implements ExecutionContext {
  private final long executionId;
  private final QueryExecution queryExecution;

  private final ObjectMapper objectMapper = OpenLineageClient.createMapper();

  private MarquezContext marquezContext;
  private final List<PartialFunction<LogicalPlan, List<Dataset>>> outputDatasetSupplier;
  private final List<PartialFunction<LogicalPlan, List<Dataset>>> inputDatasetSupplier;

  public SparkSQLExecutionContext(
      long executionId,
      MarquezContext marquezContext,
      List<PartialFunction<LogicalPlan, List<Dataset>>> outputDatasetSupplier,
      List<PartialFunction<LogicalPlan, List<Dataset>>> inputDatasetSupplier) {
    this.executionId = executionId;
    this.marquezContext = marquezContext;
    this.queryExecution = SQLExecution.getQueryExecution(executionId);
    this.outputDatasetSupplier = outputDatasetSupplier;
    this.inputDatasetSupplier = inputDatasetSupplier;
  }

  public void start(SparkListenerSQLExecutionStart startEvent) {}

  public void end(SparkListenerSQLExecutionEnd endEvent) {}

  @Override
  public void setActiveJob(ActiveJob activeJob) {}

  @Override
  public void start(SparkListenerJobStart jobStart) {
    log.info("Starting job as part of spark-sql:" + jobStart.jobId());
    if (queryExecution == null) {
      log.info("No execution info {}", queryExecution);
      return;
    }
    List<Dataset> outputDatasets =
        PlanUtils.applyFirst(outputDatasetSupplier, queryExecution.logical());
    List<Dataset> inputDatasets =
        JavaConversions.seqAsJavaList(
                queryExecution.logical().collect(PlanUtils.merge(inputDatasetSupplier)))
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    LineageEvent event =
        LineageEvent.builder()
            .inputs(inputDatasets)
            .outputs(outputDatasets)
            .run(
                buildRun(
                    buildRunFacets(
                        buildLogicalPlanFacet(queryExecution.logical()), null, buildParentFacet())))
            .job(buildJob())
            .eventTime(toZonedTime(jobStart.time()))
            .eventType("START")
            .producer("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client")
            .build();

    marquezContext.emit(event);
  }

  private ParentRunFacet buildParentFacet() {
    return ParentRunFacet.builder()
        ._producer(URI.create("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client"))
        ._schemaURL(
            URI.create(
                "https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/spec/OpenLineage.yml#ParentRunFacet"))
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
    if (queryExecution == null) {
      log.info("No execution info {}", queryExecution);
      return;
    }
    log.debug("Traversing logical plan {}", queryExecution.logical().toJSON());
    log.debug("Physical plan executed {}", queryExecution.executedPlan().toJSON());
    List<Dataset> outputDatasets =
        PlanUtils.applyFirst(outputDatasetSupplier, queryExecution.logical());
    List<Dataset> inputDatasets =
        JavaConversions.seqAsJavaList(
                queryExecution.logical().collect(PlanUtils.merge(inputDatasetSupplier)))
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    LineageEvent event =
        LineageEvent.builder()
            .inputs(inputDatasets)
            .outputs(outputDatasets)
            .run(
                buildRun(
                    buildRunFacets(
                        buildLogicalPlanFacet(queryExecution.logical()),
                        buildJobErrorFacet(jobEnd.jobResult()),
                        buildParentFacet())))
            .job(buildJob())
            .eventTime(toZonedTime(jobEnd.time()))
            .eventType(getEventType(jobEnd.jobResult()))
            .producer("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client")
            .build();

    marquezContext.emit(event);
  }

  protected ZonedDateTime toZonedTime(long time) {
    Instant i = Instant.ofEpochMilli(time);
    return ZonedDateTime.ofInstant(i, ZoneOffset.UTC);
  }

  protected String getEventType(JobResult jobResult) {
    if (jobResult.getClass().getSimpleName().startsWith("JobSucceeded")) {
      return "COMPLETE";
    }
    return "FAIL";
  }

  protected LineageEvent.Run buildRun(RunFacet facets) {
    return LineageEvent.Run.builder().runId(marquezContext.getParentRunId()).facets(facets).build();
  }

  protected RunFacet buildRunFacets(
      LogicalPlanFacet logicalPlanFacet, ErrorFacet jobError, ParentRunFacet parentRunFacet) {
    Map<String, Object> additionalFacets = new HashMap<>();
    if (logicalPlanFacet != null) {
      additionalFacets.put("spark.logicalPlan", logicalPlanFacet);
    }
    if (jobError != null) {
      additionalFacets.put("spark.exception", jobError);
    }
    return RunFacet.builder().parent(parentRunFacet).additional(additionalFacets).build();
  }

  protected LogicalPlanFacet buildLogicalPlanFacet(LogicalPlan plan) {
    return LogicalPlanFacet.builder().plan(plan).build();
  }

  protected ErrorFacet buildJobErrorFacet(JobResult jobResult) {
    if (jobResult instanceof JobFailed && ((JobFailed) jobResult).exception() != null) {
      return ErrorFacet.builder().exception(((JobFailed) jobResult).exception()).build();
    }
    return null;
  }

  protected LineageEvent.Job buildJob() {
    return LineageEvent.Job.builder()
        .namespace(marquezContext.getJobNamespace())
        .name(marquezContext.getJobName())
        .build();
  }
}
