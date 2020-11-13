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
import static java.util.stream.Collectors.groupingBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.RunDao;
import marquez.db.models.DatasetRow;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Version;

@Slf4j
public class JobService {
  private final NamespaceDao namespaceDao;
  private final DatasetDao datasetDao;
  private final JobDao jobDao;
  private final JobVersionDao jobVersionDao;
  private final JobContextDao jobContextDao;
  private final RunDao runDao;
  private final RunService runService;

  public JobService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final DatasetDao datasetDao,
      @NonNull final JobDao jobDao,
      @NonNull final JobVersionDao versionDao,
      @NonNull final JobContextDao contextDao,
      @NonNull final RunDao runDao,
      @NonNull final RunService runService) {
    this.namespaceDao = namespaceDao;
    this.datasetDao = datasetDao;
    this.jobDao = jobDao;
    this.jobVersionDao = versionDao;
    this.jobContextDao = contextDao;
    this.runDao = runDao;
    this.runService = runService;
  }

  public Job createOrUpdate(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull JobMeta jobMeta)
      throws MarquezServiceException {
    JobRow job = getOrCreateJobRow(namespaceName, jobName, jobMeta);

    final Version jobVersion = jobMeta.version(namespaceName, jobName);
    if (!jobVersionDao.exists(jobVersion.getValue())) {
      createJobVersion(job.getUuid(), jobVersion, namespaceName, jobName, jobMeta);
      // Get a new job as versions have been attached
      return getJob(namespaceName, jobName).get();
    }
    return toJob(job);
  }

  private JobRow getOrCreateJobRow(NamespaceName namespaceName, JobName jobName, JobMeta jobMeta) {
    Optional<JobRow> jobRow = jobDao.find(namespaceName.getValue(), jobName.getValue());
    if (jobRow.isEmpty()) {
      return createJobRow(namespaceName, jobName, jobMeta);
    }
    return jobRow.get();
  }

  private void createJobVersion(
      UUID jobId,
      Version jobVersion,
      NamespaceName namespaceName,
      JobName jobName,
      JobMeta jobMeta) {
    log.info("Creating version '{}' for job '{}'...", jobVersion.getValue(), jobName.getValue());

    JobContextRow contextRow = getOrCreateJobContextRow(jobMeta.getContext());

    final List<DatasetRow> inputRows = findDatasetRows(jobMeta.getInputs());
    final List<DatasetRow> outputRows = findDatasetRows(jobMeta.getOutputs());
    JobVersionRow newJobVersionRow =
        createJobVersionRow(
            jobId,
            contextRow.getUuid(),
            mapDatasetToUuid(inputRows),
            mapDatasetToUuid(outputRows),
            jobMeta.getLocation().orElse(null),
            jobVersion);

    jobMeta
        .getRunId()
        .ifPresent(
            runId -> {
              // associate new input datasets with job version
              runService.updateRunInputDatasets(
                  runId.getValue(), inputRows, namespaceName, jobName, newJobVersionRow);

              // associate the new job version with the existing job run
              final Instant updatedAt = Instant.now();
              runService.updateJobVersionUuid(
                  runId.getValue(), updatedAt, newJobVersionRow.getUuid());
            });

    JobMetrics.emitVersionMetric(
        namespaceName.getValue(), jobMeta.getType().toString(), jobName.getValue());

    log.info(
        "Successfully created version '{}' for job '{}'.",
        jobVersion.getValue(),
        jobName.getValue());
  }

  private JobContextRow getOrCreateJobContextRow(ImmutableMap<String, String> context) {
    final String checksum = Utils.checksumFor(context);
    if (!jobContextDao.exists(checksum)) {
      createJobContextRow(context, checksum);
    }

    return jobContextDao.findBy(checksum).get();
  }

  private NamespaceRow getNamespace(String namespaceName) {
    return namespaceDao.findBy(namespaceName).orElseThrow(MarquezServiceException::new);
  }

  private JobRow createJobRow(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull JobMeta jobMeta) {
    log.info(
        "No job with name '{}' for namespace '{}' found, creating...",
        jobName.getValue(),
        namespaceName.getValue());
    final NamespaceRow namespaceRow = getNamespace(namespaceName.getValue());
    final JobRow newJobRow = Mapper.toJobRow(namespaceRow, jobName, jobMeta);
    jobDao.insert(newJobRow);

    JobMetrics.emitJobCreationMetric(namespaceName.getValue(), jobMeta.getType().toString());

    log.info(
        "Successfully created job '{}' for namespace '{}' with meta: {}",
        jobName.getValue(),
        namespaceName.getValue(),
        jobMeta);
    return jobDao.find(namespaceName.getValue(), jobName.getValue()).get();
  }

  private JobVersionRow createJobVersionRow(
      UUID jobRowId,
      UUID contextRowId,
      List<UUID> input,
      List<UUID> output,
      URL location,
      Version jobVersion) {
    final JobVersionRow newJobVersionRow =
        Mapper.toJobVersionRow(jobRowId, contextRowId, input, output, location, jobVersion);
    jobVersionDao.insert(newJobVersionRow);

    return newJobVersionRow;
  }

  private void createJobContextRow(ImmutableMap<String, String> context, String checksum) {
    final JobContextRow newContextRow = Mapper.toJobContextRow(context, checksum);
    jobContextDao.insert(newContextRow);
  }

  private List<UUID> mapDatasetToUuid(List<DatasetRow> datasets) {
    return datasets.stream().map(DatasetRow::getUuid).collect(Collectors.toList());
  }

  /**
   * find the uuids for the datsets based on their namespace/name
   *
   * @param datasetIds the namespace/name ids of the datasets
   * @return their uuids
   */
  private List<DatasetRow> findDatasetRows(ImmutableSet<DatasetId> datasetIds) {
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
              return results.stream();
            })
        .collect(toImmutableList());
  }

  public boolean exists(@NonNull NamespaceName namespaceName, @NonNull JobName jobName)
      throws MarquezServiceException {
    return jobDao.exists(namespaceName.getValue(), jobName.getValue());
  }

  public Optional<Job> getJob(@NonNull NamespaceName namespaceName, @NonNull JobName jobName)
      throws MarquezServiceException {
    return jobDao.find(namespaceName.getValue(), jobName.getValue()).map(this::toJob);
  }

  public Optional<Job> getByJobVersion(@NonNull JobVersionId jobVersionId)
      throws MarquezServiceException {
    return jobDao
        .find(jobVersionId.getNamespace().getValue(), jobVersionId.getName().getValue())
        .map(jobRow -> toJob(jobRow, jobVersionId.getVersionUuid()));
  }

  public ImmutableList<Job> getAll(@NonNull NamespaceName namespaceName, int limit, int offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    final ImmutableList.Builder<Job> jobs = ImmutableList.builder();
    final List<JobRow> jobRows = jobDao.findAll(namespaceName.getValue(), limit, offset);
    for (final JobRow jobRow : jobRows) {
      jobs.add(toJob(jobRow));
    }
    return jobs.build();
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
}
