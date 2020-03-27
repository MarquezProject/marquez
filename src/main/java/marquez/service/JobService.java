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
import static java.util.Collections.emptyList;

import com.google.common.collect.ImmutableList;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
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
import marquez.service.models.DatasetVersionId;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.JobVersionId;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
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
  private final JobVersionDao versionDao;
  private final JobContextDao contextDao;
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
    this.versionDao = versionDao;
    this.contextDao = contextDao;
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
        final JobRow newJobRow = Mapper.toJobRow(namespaceRow.getUuid(), jobName, jobMeta);
        jobDao.insert(newJobRow);
        jobs.labels(namespaceName.getValue(), jobMeta.getType().toString()).inc();
        log.info(
            "Successfully created job '{}' for namespace '{}' with meta: {}",
            jobName.getValue(),
            namespaceName.getValue(),
            jobMeta);
      }
      final UUID version = jobMeta.version(namespaceName, jobName);
      if (!versionDao.exists(version)) {
        log.info("Creating version '{}' for job '{}'...", version, jobName.getValue());
        final String checksum = Utils.checksumFor(jobMeta.getContext());
        if (!contextDao.exists(checksum)) {
          final JobContextRow newContextRow =
              Mapper.toJobContextRow(jobMeta.getContext(), checksum);
          contextDao.insert(newContextRow);
        }
        final JobRow jobRow = jobDao.find(namespaceName.getValue(), jobName.getValue()).get();
        final JobContextRow contextRow = contextDao.findBy(checksum).get();
        final List<UUID> inputUuids =
            datasetDao
                .findAllIn(
                    namespaceName.getValue(),
                    jobMeta.getInputs().stream().map(DatasetName::getValue).toArray(String[]::new))
                .stream()
                .map(DatasetRow::getUuid)
                .collect(toImmutableList());
        final List<UUID> outputUuids =
            datasetDao
                .findAllIn(
                    namespaceName.getValue(),
                    jobMeta.getOutputs().stream().map(DatasetName::getValue).toArray(String[]::new))
                .stream()
                .map(DatasetRow::getUuid)
                .collect(toImmutableList());
        final JobVersionRow newVersionRow =
            Mapper.toJobVersionRow(
                jobRow.getUuid(),
                contextRow.getUuid(),
                inputUuids,
                outputUuids,
                jobMeta.getLocation().orElse(null),
                version);
        versionDao.insert(newVersionRow);
        versions
            .labels(namespaceName.getValue(), jobMeta.getType().toString(), jobName.getValue())
            .inc();
        log.info("Successfully created version '{}' for job '{}'.", version, jobName.getValue());
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

  public List<Job> getAll(@NonNull NamespaceName namespaceName, int limit, int offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final List<JobRow> jobRows = jobDao.findAll(namespaceName.getValue(), limit, offset);
      final ImmutableList.Builder<Job> builder = ImmutableList.builder();
      jobRows.forEach(
          jobRow -> {
            builder.add(toJob(jobRow));
          });
      return builder.build();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get jobs for namespace '{}'.", namespaceName.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  /**
   * Creates a {@link Job} instance from the given {@link JobRow}.
   *
   * @param jobRow
   * @return a Job
   * @throws MarquezServiceException
   */
  private Job toJob(@NonNull JobRow jobRow) {
    UUID currentVersionUuid = jobRow.getCurrentVersionUuid().get();
    final ExtendedJobVersionRow versionRow = versionDao.findBy(currentVersionUuid).get();
    final List<DatasetName> inputs =
        datasetDao.findAllIn(versionRow.getInputUuids()).stream()
            .map(row -> DatasetName.of(row.getName()))
            .collect(toImmutableList());
    final List<DatasetName> outputs =
        datasetDao.findAllIn(versionRow.getOutputUuids()).stream()
            .map(row -> DatasetName.of(row.getName()))
            .collect(toImmutableList());
    final ExtendedRunRow runRow =
        versionRow
            .getLatestRunUuid()
            .map(latestRunUuid -> runDao.findBy(latestRunUuid).get())
            .orElse(null);
    return Mapper.toJob(
        jobRow,
        inputs,
        outputs,
        versionRow.getLocation().orElse(null),
        versionRow.getContext(),
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
          versionDao.findLatest(namespaceName.getValue(), jobName.getValue()).get();
      final RunArgsRow runArgsRow = runArgsDao.findBy(checksum).get();
      final List<RunInput> inputVersions =
          datasetDao.findAllExtendedIn(versionRow.getInputUuids()).stream()
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
              .map((i) -> i.getDatasetVersion().getVersion())
              .collect(toImmutableList());
      final RunRow newRunRow =
          Mapper.toRunRow(versionRow.getUuid(), runArgsRow.getUuid(), inputVersionUuids, runMeta);
      runDao.insert(newRunRow);
      notify(
          new JobInputUpdate(
              newRunRow.getUuid(),
              runMeta,
              new JobVersionId(namespaceName, jobName, versionRow.getUuid()),
              inputVersions));
      markRunAs(newRunRow.getUuid(), Run.State.NEW);
      log.info(
          "Successfully created run '{}' for job version '{}'.",
          newRunRow.getUuid(),
          newRunRow.getJobVersionUuid());
      return getRun(newRunRow.getUuid()).get();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create run for job '{}' with meta: {}", jobName.getValue(), runMeta, e);
      throw new MarquezServiceException();
    }
  }

  public boolean runExists(@NonNull UUID runId) throws MarquezServiceException {
    try {
      return runDao.exists(runId);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check run '{}'.", runId, e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Run> getRun(UUID runId) throws MarquezServiceException {
    try {
      return runDao.findBy(runId).map(Mapper::toRun);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get run '{}'.", runId, e);
      throw new MarquezServiceException();
    }
  }

  public List<Run> getAllRunsFor(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, int limit, int offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final Optional<ExtendedJobVersionRow> versionRow =
          versionDao.findLatest(namespaceName.getValue(), jobName.getValue());
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

  public void markRunAs(@NonNull UUID runId, @NonNull Run.State runState)
      throws MarquezServiceException {
    log.debug("Marking run with ID '{}' as '{}'...", runId, runState);
    final RunStateRow newRunStateRow = Mapper.toRunStateRow(runId, runState);
    try {
      @NonNull final List<UUID> outputUuids;
      if (runState.isComplete()) {
        final RunRow runRow = runDao.findBy(runId).get();
        final ExtendedJobVersionRow versionRow =
            versionDao.findBy(runRow.getJobVersionUuid()).get();
        List<ExtendedDatasetVersionRow> outputVersions = datasetVersionDao.findByRunId(runId);
        List<RunOutput> outputs =
            outputVersions.stream()
                .map(
                    (v) ->
                        new RunOutput(
                            new DatasetVersionId(
                                NamespaceName.of(v.getNamespaceName()),
                                DatasetName.of(v.getDatasetName()),
                                v.getUuid())))
                .collect(toImmutableList());
        notify(new JobOutputUpdate(runId, outputs));
        if (versionRow.hasOutputUuids()) {
          outputUuids = versionRow.getOutputUuids();
          log.info(
              "Run '{}' for job version '{}' modified datasets: {}",
              runId,
              versionRow.getVersion(),
              outputUuids);
        } else {
          outputUuids = null;
        }
      } else {
        outputUuids = null;
      }
      runStateDao.insert(newRunStateRow, outputUuids, runState.isStarting(), runState.isComplete());
      notify(new RunTransition(runId, runState));
      incOrDecBy(runState);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to mark job run '{}' as '{}'.", runId, runState, e);
      throw new MarquezServiceException();
    }
  }

  /** Determines whether to increment or decrement run counters given {@link Run.State}. */
  private void incOrDecBy(@NonNull Run.State runState) {
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
