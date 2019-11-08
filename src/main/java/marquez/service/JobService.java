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

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.RunStateRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class JobService {
  private final NamespaceDao namespaceDao;
  private final DatasetDao datasetDao;
  private final JobDao jobDao;
  private final JobVersionDao versionDao;
  private final JobContextDao contextDao;
  private final RunDao runDao;
  private final RunArgsDao runArgsDao;
  private final RunStateDao runStateDao;

  public JobService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final DatasetDao datasetDao,
      @NonNull final JobDao jobDao,
      @NonNull final JobVersionDao versionDao,
      @NonNull final JobContextDao contextDao,
      @NonNull final RunDao runDao,
      @NonNull final RunArgsDao runArgsDao,
      @NonNull final RunStateDao runStateDao) {
    this.namespaceDao = namespaceDao;
    this.datasetDao = datasetDao;
    this.jobDao = jobDao;
    this.versionDao = versionDao;
    this.contextDao = contextDao;
    this.runDao = runDao;
    this.runArgsDao = runArgsDao;
    this.runStateDao = runStateDao;
  }

  public Job createOrUpdate(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull JobMeta meta)
      throws MarquezServiceException {
    try {
      if (!exists(namespaceName, jobName)) {
        final NamespaceRow namespaceRow = namespaceDao.findBy(namespaceName.getValue()).get();
        final JobRow newJobRow = Mapper.toJobRow(namespaceRow.getUuid(), jobName, meta);
        jobDao.insert(newJobRow);
      }

      final UUID version = meta.version(namespaceName, jobName);
      if (!versionDao.exists(version)) {
        final String checksum = Utils.checksumFor(meta.getContext());
        if (!contextDao.exists(checksum)) {
          final JobContextRow newContextRow = Mapper.toJobContextRow(meta.getContext(), checksum);
          contextDao.insert(newContextRow);
        }

        final JobRow jobRow = jobDao.findBy(namespaceName.getValue(), jobName.getValue()).get();
        final List<UUID> inputs =
            datasetDao
                .findAllInNameList(
                    meta.getInputs().stream()
                        .map(input -> input.getValue())
                        .collect(toImmutableList()))
                .stream()
                .map(row -> row.getUuid())
                .collect(toImmutableList());
        final List<UUID> outputs =
            datasetDao
                .findAllInNameList(
                    meta.getOutputs().stream()
                        .map(input -> input.getValue())
                        .collect(toImmutableList()))
                .stream()
                .map(row -> row.getUuid())
                .collect(toImmutableList());

        final JobContextRow contextRow = contextDao.findBy(checksum).get();
        final JobVersionRow newVersionRow =
            Mapper.toJobVersionRow(
                jobRow.getUuid(),
                contextRow.getUuid(),
                inputs,
                outputs,
                meta.getLocation().orElse(null),
                version);

        versionDao.insertAndUpdate(newVersionRow);
      }

      return get(namespaceName, jobName).get();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to create or update job {} for namespace {} with meta: {}",
          jobName.getValue(),
          namespaceName.getValue(),
          meta,
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
          "Failed to check job {} for namespace {}.",
          jobName.getValue(),
          namespaceName.getValue(),
          e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Job> get(@NonNull NamespaceName namespaceName, @NonNull JobName jobName)
      throws MarquezServiceException {
    try {
      Optional<JobRow> jobRow = jobDao.findBy(namespaceName.getValue(), jobName.getValue());
      if (jobRow.isPresent()) {
        return Optional.of(toJob(jobRow.get()));
      }
      return Optional.empty();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to get job {} for namespace {}.",
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
      log.error(
          "Failed to get jobs for namespace {}:  limit={} and offset={}",
          namespaceName.getValue(),
          limit,
          offset,
          e);
      throw new MarquezServiceException();
    }
  }

  private Job toJob(@NonNull JobRow jobRow) {
    final UUID currentVersionUuid = jobRow.getCurrentVersionUuid().get();
    final ExtendedJobVersionRow versionRow = versionDao.findBy(currentVersionUuid).get();

    final List<DatasetName> inputs =
        datasetDao.findAllInUuidList(versionRow.getInputs()).stream()
            .map(row -> DatasetName.of(row.getName()))
            .collect(toImmutableList());

    final List<DatasetName> outputs =
        datasetDao.findAllInUuidList(versionRow.getOutputs()).stream()
            .map(row -> DatasetName.of(row.getName()))
            .collect(toImmutableList());

    final JobContextRow contextRow = contextDao.findBy(versionRow.getJobContextUuid()).get();
    return Mapper.toJob(
        jobRow, inputs, outputs, versionRow.getLocation().orElse(null), versionRow.getContext());
  }

  public Run createRun(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull RunMeta runMeta)
      throws MarquezServiceException {
    try {
      final String checksum = Utils.checksumFor(runMeta.getArgs());
      if (!runArgsDao.exists(checksum)) {
        final RunArgsRow newRunArgsRow = Mapper.toRunArgsRow(runMeta.getArgs(), checksum);
        runArgsDao.insert(newRunArgsRow);
      }

      final JobVersionRow versionRow =
          versionDao.findLatest(namespaceName.getValue(), jobName.getValue()).get();
      final RunArgsRow runArgsRow = runArgsDao.findBy(checksum).get();
      final RunRow newRunRow = Mapper.toRunRow(versionRow.getUuid(), runArgsRow.getUuid(), runMeta);

      runDao.insertAndUpdate(newRunRow);
      markRunAs(newRunRow.getUuid(), Run.State.NEW);

      return getRun(newRunRow.getUuid()).get();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to create run for job {} for namespace {} with meta: ",
          jobName.getValue(),
          namespaceName.getValue(),
          runMeta,
          e);
      throw new MarquezServiceException();
    }
  }

  public boolean runExists(@NonNull UUID runId) throws MarquezServiceException {
    try {
      return runDao.exists(runId);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check job run {}.", runId, e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Run> getRun(UUID runId) throws MarquezServiceException {
    try {
      return runDao.findBy(runId).map(Mapper::toRun);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get job run {}.", runId, e);
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
        final List<Run> runs = Mapper.toRun(runRows);
        return ImmutableList.copyOf(runs);
      }
      return ImmutableList.of();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to get runs for job {} for namespace {}: limit={} and offset={}",
          jobName.getValue(),
          namespaceName.getValue(),
          limit,
          offset,
          e);
      log.error(e.getMessage(), e);
      throw new MarquezServiceException();
    }
  }

  public void markRunAs(@NonNull UUID runId, @NonNull Run.State runState)
      throws MarquezServiceException {
    try {
      final RunStateRow newRunStateRow = Mapper.toRunStateRow(runId, runState);
      runStateDao.insertAndUpdate(newRunStateRow);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to mark job run {} as {}.", runId, runState, e);
      throw new MarquezServiceException();
    }
  }
}
