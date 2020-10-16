/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static marquez.common.models.RunState.NEW;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.db.DatasetDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.models.DatasetRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceRow;
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
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import marquez.service.models.Version;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class JobService {
  private static final Counter jobs =
      Counter.build()
          .namespace("marquez")
          .name("job_total")
          .labelNames("namespace_name", "job_type")
          .help("Total number of jobs.")
          .register();
  private static final Counter versions =
      Counter.build()
          .namespace("marquez")
          .name("job_versions_total")
          .labelNames("namespace_name", "job_type", "job_name")
          .help("Total number of job versions.")
          .register();
  private static final Gauge runsActive =
      Gauge.build()
          .namespace("marquez")
          .name("job_runs_active")
          .help("Total number of active job runs.")
          .register();
  private static final Gauge runsCompleted =
      Gauge.build()
          .namespace("marquez")
          .name("job_runs_completed")
          .help("Total number of completed job runs.")
          .register();

  private final NamespaceDao namespaceDao;
  private final DatasetDao datasetDao;
  private final DatasetVersionDao datasetVersionDao;
  private final JobDao jobDao;
  private final JobVersionDao jobVersionDao;
  private final JobContextDao jobContextDao;
  private final RunDao runDao;
  private final RunArgsDao runArgsDao;
  private final RunStateDao runStateDao;
  private final Collection<RunTransitionListener> runTransitionListeners;

  public JobService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final DatasetDao datasetDao,
      @NonNull final DatasetVersionDao datasetVersionDao,
      @NonNull final JobDao jobDao,
      @NonNull final JobVersionDao versionDao,
      @NonNull final JobContextDao contextDao,
      @NonNull final RunDao runDao,
      @NonNull final RunArgsDao runArgsDao,
      @NonNull final RunStateDao runStateDao) {
    this(
        namespaceDao,
        datasetDao,
        datasetVersionDao,
        jobDao,
        versionDao,
        contextDao,
        runDao,
        runArgsDao,
        runStateDao,
        emptyList());
  }

  public JobService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final DatasetDao datasetDao,
      @NonNull final DatasetVersionDao datasetVersionDao,
      @NonNull final JobDao jobDao,
      @NonNull final JobVersionDao versionDao,
      @NonNull final JobContextDao contextDao,
      @NonNull final RunDao runDao,
      @NonNull final RunArgsDao runArgsDao,
      @NonNull final RunStateDao runStateDao,
      @Nullable final Collection<RunTransitionListener> runTransitionListeners) {
    this.namespaceDao = namespaceDao;
    this.datasetDao = datasetDao;
    this.datasetVersionDao = datasetVersionDao;
    this.jobDao = jobDao;
    this.jobVersionDao = versionDao;
    this.jobContextDao = contextDao;
    this.runDao = runDao;
    this.runArgsDao = runArgsDao;
    this.runStateDao = runStateDao;
    this.runTransitionListeners = runTransitionListeners;
  }

  public Job createOrUpdate(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull JobMeta jobMeta)
      throws MarquezServiceException {
    try {
      if (!exists(namespaceName, jobName)) {
        log.info(
            "No job with name '{}' for namespace '{}' found, creating...",
            jobName.getValue(),
            namespaceName.getValue());
        final NamespaceRow namespaceRow = namespaceDao.findBy(namespaceName.getValue()).get();
        final JobRow newJobRow = Mapper.toJobRow(namespaceRow, jobName, jobMeta);
        jobDao.insert(newJobRow);
        jobs.labels(namespaceName.getValue(), jobMeta.getType().toString()).inc();
        log.info(
            "Successfully created job '{}' for namespace '{}' with meta: {}",
            jobName.getValue(),
            namespaceName.getValue(),
            jobMeta);
      }

      final Version jobVersion = jobMeta.version(namespaceName, jobName);
      if (!jobVersionDao.exists(jobVersion.getValue())) {
        log.info(
            "Creating version '{}' for job '{}'...", jobVersion.getValue(), jobName.getValue());
        final String checksum = Utils.checksumFor(jobMeta.getContext());
        if (!jobContextDao.exists(checksum)) {
          final JobContextRow newContextRow =
              Mapper.toJobContextRow(jobMeta.getContext(), checksum);
          jobContextDao.insert(newContextRow);
        }
        final JobRow jobRow = jobDao.find(namespaceName.getValue(), jobName.getValue()).get();
        final JobContextRow contextRow = jobContextDao.findBy(checksum).get();
        final List<UUID> inputUuids = findUuids(jobMeta.getInputs());
        final List<UUID> outputUuids = findUuids(jobMeta.getOutputs());
        final JobVersionRow newJobVersionRow =
            Mapper.toJobVersionRow(
                jobRow.getUuid(),
                contextRow.getUuid(),
                inputUuids,
                outputUuids,
                jobMeta.getLocation().orElse(null),
                jobVersion);
        jobVersionDao.insert(newJobVersionRow);
        versions
            .labels(namespaceName.getValue(), jobMeta.getType().toString(), jobName.getValue())
            .inc();
        log.info(
            "Successfully created version '{}' for job '{}'.",
            jobVersion.getValue(),
            jobName.getValue());

        // When a run ID is present, associate the new job version with the existing job run.
        jobMeta
            .getRunId()
            .ifPresent(
                runId -> {
                  final ExtendedRunRow runRow = runDao.findBy(runId.getValue()).get();
                  final Instant updatedAt = Instant.now();
                  runDao.updateJobVersionUuid(
                      runRow.getUuid(), updatedAt, newJobVersionRow.getUuid());
                  log.info(
                      "Successfully associated run '{}' with version '{}'.",
                      runId.getValue(),
                      jobVersion.getValue());
                });
      }
      return get(namespaceName, jobName).get();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to create or update job '{}' for namespace '{}' with meta: {}",
          jobName.getValue(),
          namespaceName.getValue(),
          jobMeta,
          e);
      throw new MarquezServiceException();
    }
  }

  /**
   * find the uuids for the datsets based on their namespace/name
   *
   * @param datasetIds the namespace/name ids of the datasets
   * @return their uuids
   */
  private List<UUID> findUuids(ImmutableSet<DatasetId> datasetIds) {
    // group per namespace since that's how we can query the db
    Map<@NonNull NamespaceName, List<DatasetId>> byNamespace =
        datasetIds.stream().collect(groupingBy(DatasetId::getNamespace));
    // query the db for all ds uuids for each namespace and combine them back in one list
    return byNamespace.entrySet().stream()
        .flatMap(
            (e) -> {
              String namespace = e.getKey().getValue();
              List<String> names =
                  e.getValue().stream()
                      .map(datasetId -> datasetId.getName().getValue())
                      .collect(toImmutableList());
              List<DatasetRow> results = datasetDao.findAllIn(namespace, names);
              if (results.size() < names.size()) {
                List<String> actual =
                    results.stream().map(DatasetRow::getName).collect(toImmutableList());
                throw new MarquezServiceException(
                    String.format(
                        "Some datasets not found in namespace %s \nExpected: %s\nActual: %s",
                        namespace, names, actual));
              }
              return results.stream().map(DatasetRow::getUuid);
            })
        .collect(toImmutableList());
  }

  public boolean exists(@NonNull NamespaceName namespaceName, @NonNull JobName jobName)
      throws MarquezServiceException {
    try {
      return jobDao.exists(namespaceName.getValue(), jobName.getValue());
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to check job '{}' for namespace '{}'.",
          jobName.getValue(),
          namespaceName.getValue(),
          e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Job> get(@NonNull NamespaceName namespaceName, @NonNull JobName jobName)
      throws MarquezServiceException {
    try {
      return jobDao.find(namespaceName.getValue(), jobName.getValue()).map(this::toJob);
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to get job '{}' for namespace '{}'.",
          jobName.getValue(),
          namespaceName.getValue(),
          e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Job> getBy(@NonNull JobVersionId jobVersionId) throws MarquezServiceException {
    try {
      return jobDao
          .find(jobVersionId.getNamespace().getValue(), jobVersionId.getName().getValue())
          .map(jobRow -> toJob(jobRow, jobVersionId.getVersionUuid()));
    } catch (UnableToExecuteStatementException e) {
      throw new MarquezServiceException(
          String.format("Failed to get job version: '%s'.", jobVersionId), e);
    }
  }

  public ImmutableList<Job> getAll(@NonNull NamespaceName namespaceName, int limit, int offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final ImmutableList.Builder<Job> jobs = ImmutableList.builder();
      final List<JobRow> jobRows = jobDao.findAll(namespaceName.getValue(), limit, offset);
      for (final JobRow jobRow : jobRows) {
        jobs.add(toJob(jobRow));
      }
      return jobs.build();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get jobs for namespace '{}'.", namespaceName.getValue(), e);
      throw new MarquezServiceException(e);
    }
  }

  /** Creates a {@link Job} instance from the given {@link JobRow}. */
  private Job toJob(@NonNull JobRow jobRow) {
    return toJob(jobRow, null);
  }

  private Job toJob(@NonNull JobRow jobRow, @Nullable UUID jobVersionUuid) {
    final UUID currentJobVersionUuid =
        (jobVersionUuid == null)
            ? jobRow
                .getCurrentVersionUuid()
                .orElseThrow(
                    () ->
                        new MarquezServiceException(
                            String.format("Version missing for job row '%s'.", jobRow.getUuid())))
            : jobVersionUuid;
    final ExtendedJobVersionRow jobVersionRow =
        jobVersionDao
            .findBy(currentJobVersionUuid)
            .orElseThrow(
                () ->
                    new MarquezServiceException(
                        String.format(
                            "Version '%s' not found for job row '%s'.",
                            currentJobVersionUuid, jobRow)));
    final ImmutableSet<DatasetId> inputs =
        datasetDao.findAllIn(jobVersionRow.getInputUuids()).stream()
            .map(Mapper::toDatasetId)
            .collect(toImmutableSet());
    final ImmutableSet<DatasetId> outputs =
        datasetDao.findAllIn(jobVersionRow.getOutputUuids()).stream()
            .map(Mapper::toDatasetId)
            .collect(toImmutableSet());
    final ExtendedRunRow runRow =
        jobVersionRow
            .getLatestRunUuid()
            .map(latestRunUuid -> runDao.findBy(latestRunUuid).get())
            .orElse(null);
    return Mapper.toJob(
        jobRow,
        inputs,
        outputs,
        jobVersionRow.getLocation().orElse(null),
        jobVersionRow.getContext(),
        runRow);
  }

  public Run createRun(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull RunMeta runMeta)
      throws MarquezServiceException {
    try {
      log.info("Creating run for job '{}'...", jobName.getValue());
      final String checksum = Utils.checksumFor(runMeta.getArgs());
      if (!runArgsDao.exists(checksum)) {
        log.debug(
            "New run args with checksum '{}' for job '{}' found: {}",
            checksum,
            jobName.getValue(),
            runMeta.getArgs());
        final RunArgsRow newRunArgsRow = Mapper.toRunArgsRow(runMeta.getArgs(), checksum);
        runArgsDao.insert(newRunArgsRow);
      }
      final JobVersionRow versionRow =
          jobVersionDao.findLatest(namespaceName.getValue(), jobName.getValue()).get();
      final RunArgsRow runArgsRow = runArgsDao.findBy(checksum).get();
      final List<RunInput> inputVersions =
          datasetDao.findAllIn(versionRow.getInputUuids()).stream()
              .map(
                  (row) ->
                      new RunInput(
                          new DatasetVersionId(
                              NamespaceName.of(row.getNamespaceName()),
                              DatasetName.of(row.getName()),
                              row.getCurrentVersionUuid().get())))
              .collect(toImmutableList());
      final List<UUID> inputVersionUuids =
          inputVersions.stream()
              .map((i) -> i.getDatasetVersionId().getVersionUuid())
              .collect(toImmutableList());
      final RunRow newRunRow =
          Mapper.toRunRow(versionRow.getUuid(), runArgsRow.getUuid(), inputVersionUuids, runMeta);
      runDao.insert(newRunRow);
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
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create run for job '{}' with meta: {}", jobName.getValue(), runMeta, e);
      throw new MarquezServiceException();
    }
  }

  public boolean runExists(@NonNull RunId runId) throws MarquezServiceException {
    try {
      return runDao.exists(runId.getValue());
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check run '{}'.", runId, e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Run> getRun(RunId runId) throws MarquezServiceException {
    try {
      return runDao.findBy(runId.getValue()).map(Mapper::toRun);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get run '{}'.", runId, e);
      throw new MarquezServiceException();
    }
  }

  public ImmutableList<Run> getAllRunsFor(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, int limit, int offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final Optional<ExtendedJobVersionRow> versionRow =
          jobVersionDao.findLatest(namespaceName.getValue(), jobName.getValue());
      if (versionRow.isPresent()) {
        final List<ExtendedRunRow> runRows =
            runDao.findAll(versionRow.get().getUuid(), limit, offset);
        final List<Run> runs = Mapper.toRuns(runRows);
        return ImmutableList.copyOf(runs);
      }
      return ImmutableList.of();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to get runs for job '{}' for namespace '{}'.",
          jobName.getValue(),
          namespaceName.getValue(),
          e);
      throw new MarquezServiceException();
    }
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
    try {
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
      incOrDecBy(newRunState);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to mark job run '{}' as '{}'.", runId, newRunState, e);
      throw new MarquezServiceException();
    }
  }

  /** Determines whether to increment or decrement run counters given {@link RunState}. */
  private void incOrDecBy(@NonNull RunState runState) {
    switch (runState) {
      case NEW:
        break;
      case RUNNING:
        runsActive.inc();
        break;
      case COMPLETED:
        runsActive.dec();
        runsCompleted.inc();
        break;
      case ABORTED:
      case FAILED:
        runsActive.dec();
        break;
    }
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
