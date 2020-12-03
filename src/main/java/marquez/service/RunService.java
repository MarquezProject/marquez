package marquez.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static marquez.common.models.RunState.NEW;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.exceptions.RunNotFoundException;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.db.DatasetDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobVersionDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.RunStateRow;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.RunTransitionListener.RunInput;
import marquez.service.RunTransitionListener.RunOutput;
import marquez.service.RunTransitionListener.RunTransition;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;

@Slf4j
public class RunService {
  private final JobVersionDao jobVersionDao;
  private final DatasetDao datasetDao;
  private final RunArgsDao runArgsDao;
  private final RunDao runDao;
  private final DatasetVersionDao datasetVersionDao;
  private final RunStateDao runStateDao;
  private final Collection<RunTransitionListener> runTransitionListeners;

  public RunService(
      JobVersionDao jobVersionDao,
      DatasetDao datasetDao,
      RunArgsDao runArgsDao,
      RunDao runDao,
      DatasetVersionDao datasetVersionDao,
      RunStateDao runStateDao,
      Collection<RunTransitionListener> runTransitionListeners) {
    this.jobVersionDao = jobVersionDao;
    this.datasetDao = datasetDao;
    this.runArgsDao = runArgsDao;
    this.runDao = runDao;
    this.datasetVersionDao = datasetVersionDao;
    this.runStateDao = runStateDao;
    this.runTransitionListeners = runTransitionListeners;
  }

  public Run createRun(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull RunMeta runMeta)
      throws MarquezServiceException {
    log.info("Creating run for job '{}'...", jobName.getValue());

    final JobVersionRow versionRow =
        jobVersionDao.findLatest(namespaceName.getValue(), jobName.getValue()).get();
    final RunArgsRow runArgsRow = getOrCreateRunArgsRow(runMeta);

    final List<RunInput> inputVersions = getRunInput(versionRow.getInputUuids());
    final List<UUID> inputVersionUuids = mapRunInputToUuid(inputVersions);

    final RunRow newRunRow =
        createRunRow(versionRow.getUuid(), runArgsRow.getUuid(), inputVersionUuids, runMeta);

    notify(
        new JobInputUpdate(
            RunId.of(newRunRow.getUuid()),
            runMeta,
            new JobVersionId(namespaceName, jobName, versionRow.getUuid()),
            inputVersions));

    markRunAs(RunId.of(newRunRow.getUuid()), NEW);
    log.info(
        "Successfully created run '{}' for job version '{}'.",
        newRunRow.getUuid(),
        newRunRow.getJobVersionUuid());
    return getRun(RunId.of(newRunRow.getUuid())).get();
  }

  private RunRow createRunRow(
      UUID versionRowId, UUID runArgsRowId, List<UUID> inputVersionUuids, RunMeta runMeta) {
    final RunRow newRunRow =
        Mapper.toRunRow(versionRowId, runArgsRowId, inputVersionUuids, runMeta);
    runDao.insert(newRunRow);
    return newRunRow;
  }

  private List<UUID> mapRunInputToUuid(List<RunInput> inputVersions) {
    return inputVersions.stream()
        .map((i) -> i.getDatasetVersionId().getVersionUuid())
        .collect(toImmutableList());
  }

  private List<RunInput> getRunInput(List<UUID> inputUuids) {
    return datasetDao.findAllIn(inputUuids).stream()
        .map(
            (row) ->
                new RunInput(
                    new DatasetVersionId(
                        NamespaceName.of(row.getNamespaceName()),
                        DatasetName.of(row.getName()),
                        row.getCurrentVersionUuid().get())))
        .collect(toImmutableList());
  }

  private RunArgsRow getOrCreateRunArgsRow(RunMeta runMeta) {
    final String checksum = Utils.checksumFor(runMeta.getArgs());
    Optional<RunArgsRow> runArgsRow = runArgsDao.findBy(checksum);
    if (runArgsRow.isPresent()) {
      return runArgsRow.get();
    }

    log.debug("New run args with checksum '{}' found: {}", checksum, runMeta.getArgs());
    final RunArgsRow newRunArgsRow = Mapper.toRunArgsRow(runMeta.getArgs(), checksum);
    runArgsDao.insert(newRunArgsRow);
    return runArgsDao.findBy(checksum).get();
  }

  public boolean runExists(@NonNull RunId runId) throws MarquezServiceException {
    return runDao.exists(runId.getValue());
  }

  public ExtendedRunRow getRun(UUID runId) {
    return runDao.findBy(runId).orElseThrow(() -> new RunNotFoundException(RunId.of(runId)));
  }

  public Optional<Run> getRun(RunId runId) throws MarquezServiceException {
    return runDao.findBy(runId.getValue()).map(Mapper::toRun);
  }

  public ImmutableList<Run> getAllRunsFor(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, int limit, int offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");

    final List<ExtendedRunRow> runRows =
        runDao.findAll(namespaceName.getValue(), jobName.getValue(), limit, offset);
    final List<Run> runs = Mapper.toRuns(runRows);
    return ImmutableList.copyOf(runs);
  }

  public void markRunAs(@NonNull RunId runId, @NonNull RunState newRunState) {
    markRunAs(runId, newRunState, null);
  }

  public void markRunAs(
      @NonNull RunId runId, @NonNull RunState newRunState, @Nullable Instant transitionedAt)
      throws MarquezServiceException {
    log.debug("Marking run with ID '{}' as '{}'...", runId, newRunState);
    final RunStateRow newRunStateRow =
        Mapper.toRunStateRow(runId.getValue(), newRunState, transitionedAt);
    final RunRow runRow = runDao.findBy(runId.getValue()).get();
    final List<UUID> outputUuids;
    if (newRunState.isComplete()) {
      final ExtendedJobVersionRow jobVersionRow =
          jobVersionDao.findBy(runRow.getJobVersionUuid()).get();
      final List<ExtendedDatasetVersionRow> outputVersions =
          datasetVersionDao.findByRunId(runId.getValue());
      final List<RunOutput> outputs =
          outputVersions.stream()
              .map(
                  (v) ->
                      new RunOutput(
                          new DatasetVersionId(
                              NamespaceName.of(v.getNamespaceName()),
                              DatasetName.of(v.getDatasetName()),
                              v.getUuid())))
              .collect(toImmutableList());
      notify(
          new JobOutputUpdate(
              runId,
              new JobVersionId(
                  NamespaceName.of(jobVersionRow.getNamespaceName()),
                  JobName.of(jobVersionRow.getName()),
                  jobVersionRow.getUuid()),
              outputs));
      if (jobVersionRow.hasOutputUuids()) {
        outputUuids = jobVersionRow.getOutputUuids();
        log.info(
            "Run '{}' for job version '{}' modified datasets: {}",
            runId,
            jobVersionRow.getVersion(),
            outputUuids);
      } else {
        outputUuids = null;
      }
    } else {
      outputUuids = null;
    }
    runStateDao.insert(
        newRunStateRow,
        outputUuids,
        newRunState.isStarting(),
        newRunState.isDone(),
        newRunState.isComplete());
    final RunState oldRunState = runRow.getCurrentRunState().map(RunState::valueOf).orElse(null);
    notify(new RunTransition(runId, oldRunState, newRunState));
    JobMetrics.emitRunStateCounterMetric(newRunState);
  }

  public void updateRunInputDatasets(
      UUID runUuid,
      List<DatasetRow> inputRows,
      NamespaceName namespaceName,
      JobName jobName,
      UUID jobVersionUuid) {
    if (inputRows.isEmpty()) {
      return;
    }
    List<RunInput> runInputs = new ArrayList<>();
    for (DatasetRow inputDataset : inputRows) {
      Optional<DatasetVersionRow> version =
          datasetVersionDao.findLatestDatasetByUuid(inputDataset.getUuid());
      if (version.isPresent()) {
        DatasetVersionRow row = version.get();
        runDao.updateInputVersions(runUuid, version.get().getUuid());
        runInputs.add(
            new RunInput(
                new DatasetVersionId(
                    namespaceName, DatasetName.of(inputDataset.getName()), row.getUuid())));
      } else {
        log.error(
            "Input dataset version could not be found during run input update. Input Dataset ID: {}",
            inputDataset.getUuid());
      }
    }
    Optional<ExtendedRunRow> runRow = runDao.findBy(runUuid);
    if (runRow.isEmpty()) {
      throw new RunNotFoundException(RunId.of(runUuid));
    }
    ExtendedRunRow run = runRow.get();
    notify(
        new JobInputUpdate(
            RunId.of(runUuid),
            new RunMeta(
                RunId.of(runUuid),
                run.getNominalStartTime().orElse(null),
                run.getNominalEndTime().orElse(null),
                Mapper.toRunArgs(run.getArgs())),
            new JobVersionId(namespaceName, jobName, jobVersionUuid),
            runInputs));
  }

  public void updateJobVersionUuid(UUID runId, Instant updatedAt, @NonNull UUID newJobVersionUuid) {
    final ExtendedRunRow runRow = getRun(runId);
    runDao.updateJobVersionUuid(runRow.getUuid(), updatedAt, newJobVersionUuid);
    log.info("Successfully associated run '{}' with version '{}'.", runId, newJobVersionUuid);
  }

  private void notify(JobInputUpdate update) {
    notify(RunTransitionListener::notify, update);
  }

  private void notify(JobOutputUpdate update) {
    notify(RunTransitionListener::notify, update);
  }

  private void notify(RunTransition transition) {
    notify(RunTransitionListener::notify, transition);
  }

  private <T> void notify(BiConsumer<RunTransitionListener, T> f, T param) {
    for (RunTransitionListener runTransitionListener : runTransitionListeners) {
      try {
        f.accept(runTransitionListener, param);
      } catch (Exception e) {
        log.error("Exception from listener " + runTransitionListener, e);
      }
    }
  }
}
