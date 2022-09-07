/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static marquez.logging.MdcPropagating.withMdc;
import static marquez.tracing.SentryPropagating.withSentry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.BaseDao;
import marquez.db.DatasetDao;
import marquez.db.DatasetVersionDao;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.JobRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.UpdateLineageRow;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.RunTransitionListener.RunInput;
import marquez.service.RunTransitionListener.RunOutput;
import marquez.service.models.LineageEvent;
import marquez.service.models.RunMeta;

@Slf4j
public class OpenLineageService extends DelegatingDaos.DelegatingOpenLineageDao {
  private final RunService runService;
  private final DatasetVersionDao datasetVersionDao;
  private final ObjectMapper mapper = Utils.newObjectMapper();

  private final Executor executor;

  public OpenLineageService(BaseDao baseDao, RunService runService) {
    this(baseDao, runService, ForkJoinPool.commonPool());
  }

  public OpenLineageService(BaseDao baseDao, RunService runService, Executor executor) {
    super(baseDao.createOpenLineageDao());
    this.runService = runService;
    this.datasetVersionDao = baseDao.createDatasetVersionDao();
    this.executor = executor;
  }

  public CompletableFuture<Void> createAsync(LineageEvent event) {
    UUID runUuid = runUuidFromEvent(event.getRun());
    CompletableFuture<Void> openLineage =
        CompletableFuture.runAsync(
            withSentry(
                withMdc(
                    () ->
                        createLineageEvent(
                            event.getEventType() == null ? "" : event.getEventType(),
                            event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant(),
                            runUuid,
                            event.getJob().getName(),
                            event.getJob().getNamespace(),
                            createJsonArray(event, mapper),
                            event.getProducer()))),
            executor);

    CompletableFuture<Void> marquez =
        CompletableFuture.supplyAsync(
                withSentry(withMdc(() -> updateMarquezModel(event, mapper))), executor)
            .thenAccept(
                (update) -> {
                  if (event.getEventType() != null) {
                    if (event.getEventType().equalsIgnoreCase("COMPLETE")) {
                      buildJobOutputUpdate(update).ifPresent(runService::notify);
                    }
                    buildJobInputUpdate(update).ifPresent(runService::notify);
                  }
                });

    return CompletableFuture.allOf(marquez, openLineage);
  }

  /**
   * Try to convert the run id to a UUID. If it isn't a properly formatted UUID, generate one from
   * the string bytes
   *
   * @param run
   * @return the {@link UUID} for the run
   */
  private UUID runUuidFromEvent(LineageEvent.Run run) {
    UUID runUuid;
    try {
      runUuid = UUID.fromString(run.getRunId());
    } catch (Exception e) {
      runUuid = UUID.nameUUIDFromBytes(run.getRunId().getBytes(StandardCharsets.UTF_8));
    }
    return runUuid;
  }

  private Optional<JobOutputUpdate> buildJobOutputUpdate(UpdateLineageRow record) {
    RunId runId = RunId.of(record.getRun().getUuid());
    return buildJobOutput(runId, buildJobVersionId(record), record);
  }

  private Optional<JobInputUpdate> buildJobInputUpdate(UpdateLineageRow record) {
    RunId runId = RunId.of(record.getRun().getUuid());
    return buildJobInput(
        record.getRun(),
        record.getRunArgs(),
        record.getJob(),
        buildJobVersionId(record),
        runId,
        record);
  }

  public JobVersionId buildJobVersionId(UpdateLineageRow record) {
    if (record.getJobVersionBag() != null) {
      return JobVersionId.builder()
          .version(record.getJobVersionBag().getJobVersionRow().getUuid())
          .namespace(NamespaceName.of(record.getNamespace().getName()))
          .name(JobName.of(record.getJob().getName()))
          .build();
    }
    return null;
  }

  Optional<JobOutputUpdate> buildJobOutput(
      RunId runId, JobVersionId jobVersionId, UpdateLineageRow record) {
    // We query for all datasets since they can come in slowly over time
    List<ExtendedDatasetVersionRow> datasets =
        datasetVersionDao.findOutputDatasetVersionsFor(record.getRun().getUuid());
    DatasetDao datasetDao = createDatasetDao();
    datasets.forEach(
        versionRow ->
            datasetDao.updateVersion(
                versionRow.getDatasetUuid(), Instant.now(), versionRow.getUuid()));

    // Do not trigger a JobOutput event if there are no new datasets
    if (datasets.isEmpty() && record.getOutputs().isEmpty()) {
      return Optional.empty();
    }

    List<RunOutput> runOutputs =
        datasets.stream()
            .map(this::buildDatasetVersionId)
            .map(RunOutput::new)
            .collect(Collectors.toList());

    return Optional.of(
        new JobOutputUpdate(
            runId,
            jobVersionId,
            JobName.of(record.getJob().getName()),
            NamespaceName.of(record.getJob().getNamespaceName()),
            runOutputs));
  }

  Optional<JobInputUpdate> buildJobInput(
      RunRow run,
      RunArgsRow runArgsRow,
      JobRow jobRow,
      JobVersionId jobVersionId,
      RunId runId,
      UpdateLineageRow record) {
    // We query for all datasets since they can come in slowly over time
    List<ExtendedDatasetVersionRow> datasets =
        datasetVersionDao.findInputDatasetVersionsFor(record.getRun().getUuid());
    // Do not trigger a JobInput event if there are no new datasets
    if (datasets.isEmpty() || record.getInputs().isEmpty()) {
      return Optional.empty();
    }

    Map<String, String> runArgs;
    try {
      runArgs = Utils.fromJson(runArgsRow.getArgs(), new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      runArgs = new HashMap<>();
    }

    List<RunInput> runInputs =
        datasets.stream()
            .map(this::buildDatasetVersionId)
            .map(RunInput::new)
            .collect(Collectors.toList());

    return Optional.of(
        new JobInputUpdate(
            runId,
            RunMeta.builder()
                .id(RunId.of(run.getUuid()))
                .nominalStartTime(run.getNominalStartTime().orElse(null))
                .nominalEndTime(run.getNominalEndTime().orElse(null))
                .args(runArgs)
                .build(),
            jobVersionId,
            JobName.of(jobRow.getName()),
            NamespaceName.of(jobRow.getNamespaceName()),
            runInputs));
  }

  private DatasetVersionId buildDatasetVersionId(ExtendedDatasetVersionRow ds) {
    return DatasetVersionId.builder()
        .version(ds.getUuid())
        .namespace(NamespaceName.of(ds.getNamespaceName()))
        .name(DatasetName.of(ds.getDatasetName()))
        .build();
  }
}
