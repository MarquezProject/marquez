/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.Value;
import marquez.api.models.JobVersion;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunState;
import marquez.common.models.Version;
import marquez.db.mappers.ExtendedJobVersionRowMapper;
import marquez.db.mappers.JobVersionMapper;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.service.models.Run;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/** The DAO for {@code JobVersion}. */
@RegisterRowMapper(ExtendedJobVersionRowMapper.class)
@RegisterRowMapper(JobVersionMapper.class)
public interface JobVersionDao extends BaseDao {
  /** An {@code enum} used to determine the input / output dataset type for a given job version. */
  enum IoType {
    INPUT,
    OUTPUT
  }

  /**
   * Returns JobVersion fields, along with Run-related fields, prefixed with "run_". Input and
   * Output datasets are constructed as JSON strings that can be deserialized into DatasetIds.
   */
  String BASE_SELECT_ON_JOB_VERSIONS =
      "WITH job_version_io AS (\n"
          + "    SELECT io.job_version_uuid,\n"
          + "           JSON_AGG(json_build_object('namespace', ds.namespace_name,\n"
          + "                                      'name', ds.name))\n"
          + "           FILTER (WHERE io.io_type = 'INPUT') AS input_datasets,\n"
          + "           JSON_AGG(json_build_object('namespace', ds.namespace_name,\n"
          + "                                      'name', ds.name))\n"
          + "           FILTER (WHERE io.io_type = 'OUTPUT') AS output_datasets\n"
          + "    FROM job_versions_io_mapping io\n"
          + "    INNER JOIN job_versions jv ON jv.uuid = io.job_version_uuid\n"
          + "    INNER JOIN datasets ds ON ds.uuid = io.dataset_uuid\n"
          + "    INNER JOIN jobs_view j ON j.uuid=jv.job_uuid\n"
          + "    WHERE j.namespace_name = :namespaceName\n"
          + "      AND j.name = :jobName\n"
          + "    GROUP BY io.job_version_uuid\n"
          + "), relevant_job_versions AS (\n"
          + "    SELECT jv.uuid, jv.created_at, jv.updated_at, jv.job_uuid, jv.version, \n"
          + "    jv.location, jv.latest_run_uuid, jv.job_context_uuid, j.namespace_uuid, \n"
          + "    j.namespace_name, j.name AS job_name, jc.context\n"
          + "    FROM job_versions jv\n"
          + "    LEFT OUTER JOIN job_contexts AS jc ON jc.uuid = jv.job_context_uuid\n"
          + "    INNER JOIN jobs_view j ON j.uuid=jv.job_uuid\n"
          + "    WHERE j.name = :jobName AND j.namespace_name=:namespaceName\n"
          + "    ORDER BY jv.created_at DESC\n"
          + ")\n"
          + "SELECT jv.*,\n"
          + "       dsio.input_datasets,\n"
          + "       dsio.output_datasets,\n"
          + "       r.uuid               AS run_uuid,\n"
          + "       r.created_at         AS run_created_at,\n"
          + "       r.updated_at         AS run_updated_at,\n"
          + "       r.nominal_start_time AS run_nominal_start_time,\n"
          + "       r.nominal_end_time   AS run_nominal_end_time,\n"
          + "       r.current_run_state  AS run_current_run_state,\n"
          + "       r.started_at         AS run_started_at,\n"
          + "       r.ended_at           AS run_ended_at,\n"
          + "       r.namespace_name     AS run_namespace_name,\n"
          + "       r.job_name           AS run_job_name,\n"
          + "       jv.version           AS run_job_version,\n"
          + "       r.location           AS run_location,\n"
          + "       ra.args              AS run_args,\n"
          + "       jv.context           AS run_context,\n"
          + "       f.facets             AS run_facets,\n"
          + "       ri.input_versions    AS run_input_versions,\n"
          + "       ro.output_versions   AS run_output_versions\n"
          + "FROM relevant_job_versions AS jv\n"
          + "LEFT JOIN job_version_io dsio ON dsio.job_version_uuid = jv.uuid\n"
          + "LEFT OUTER JOIN runs r ON r.uuid = jv.latest_run_uuid\n"
          + "LEFT JOIN LATERAL (\n"
          + "    SELECT le.run_uuid, JSON_AGG(event -> 'run' -> 'facets') AS facets\n"
          + "    FROM lineage_events le\n"
          + "    WHERE le.run_uuid=jv.latest_run_uuid\n"
          + "    GROUP BY le.run_uuid\n"
          + ") AS f ON r.uuid = f.run_uuid\n"
          + "LEFT OUTER JOIN run_args AS ra ON ra.uuid = r.run_args_uuid\n"
          + "LEFT JOIN LATERAL (\n"
          + "    SELECT im.run_uuid,\n"
          + "           JSON_AGG(json_build_object('namespace', dv.namespace_name,\n"
          + "                                      'name', dv.dataset_name,\n"
          + "                                      'version', dv.version)) AS input_versions\n"
          + "    FROM runs_input_mapping im\n"
          + "    INNER JOIN dataset_versions dv on im.dataset_version_uuid = dv.uuid\n"
          + "    WHERE im.run_uuid=jv.latest_run_uuid\n"
          + "    GROUP BY im.run_uuid\n"
          + ") ri ON ri.run_uuid = r.uuid\n"
          + "LEFT OUTER JOIN (\n"
          + "    SELECT run_uuid,\n"
          + "           JSON_AGG(json_build_object('namespace', namespace_name,\n"
          + "                                      'name', dataset_name,\n"
          + "                                      'version', version)) AS output_versions\n"
          + "    FROM dataset_versions\n"
          + "    GROUP BY run_uuid\n"
          + ") ro ON ro.run_uuid = r.uuid\n";

  @SqlQuery(BASE_SELECT_ON_JOB_VERSIONS + "WHERE jv.version = :jobVersionUuid")
  Optional<JobVersion> findJobVersion(String namespaceName, String jobName, UUID jobVersionUuid);

  @SqlQuery(BASE_SELECT_ON_JOB_VERSIONS + "LIMIT :limit OFFSET :offset")
  List<JobVersion> findAllJobVersions(String namespaceName, String jobName, int limit, int offset);

  /**
   * Used to upsert a {@link JobVersionRow} object; on version conflict, the job version object is
   * returned with the {@code updated_at} column set to the last modified timestamp.
   *
   * @param jobVersionUuid The unique ID of the job version.
   * @param now The last modified timestamp of the job version.
   * @param jobUuid The unique ID of the job associated with the version.
   * @param jobContextUuid The unique ID of the job context associated with the version.
   * @param jobLocation The source code location for the job.
   * @param version The version of the job; for internal use only.
   * @param jobName The name of the job.
   * @param namespaceUuid The unique ID of the namespace associated with the job version.
   * @param namespaceName The namespace associated with the job version.
   * @return The {@link ExtendedJobVersionRow} object inserted into the {@code job_versions} table.
   */
  // TODO: A JobVersionRow object should be immutable; replace with JobVersionDao.insertJobVersion()
  @SqlQuery(
      "INSERT INTO job_versions ("
          + "uuid, "
          + "created_at, "
          + "updated_at, "
          + "job_uuid, "
          + "job_context_uuid, "
          + "location, "
          + "version, "
          + "job_name, "
          + "namespace_uuid, "
          + "namespace_name"
          + ") VALUES ("
          + ":jobVersionUuid, "
          + ":now, "
          + ":now, "
          + ":jobUuid, "
          + ":jobContextUuid, "
          + ":jobLocation, "
          + ":version, "
          + ":jobName, "
          + ":namespaceUuid, "
          + ":namespaceName) "
          + "ON CONFLICT(version) DO "
          + "UPDATE SET updated_at = EXCLUDED.updated_at "
          + "RETURNING *")
  ExtendedJobVersionRow upsertJobVersion(
      UUID jobVersionUuid,
      Instant now,
      UUID jobUuid,
      UUID jobContextUuid,
      String jobLocation,
      UUID version,
      String jobName,
      UUID namespaceUuid,
      String namespaceName);

  /**
   * Used to link an input dataset to a given job version.
   *
   * @param jobVersionUuid The unique ID of the job version.
   * @param inputDatasetUuid The unique ID of the input dataset.
   */
  default void upsertInputDatasetFor(UUID jobVersionUuid, UUID inputDatasetUuid) {
    upsertInputOrOutputDatasetFor(jobVersionUuid, inputDatasetUuid, IoType.INPUT);
  }

  /**
   * Used to link an output dataset to a given job version.
   *
   * @param jobVersionUuid The unique ID of the job version.
   * @param outputDatasetUuid The unique ID of the output dataset.
   */
  default void upsertOutputDatasetFor(UUID jobVersionUuid, UUID outputDatasetUuid) {
    upsertInputOrOutputDatasetFor(jobVersionUuid, outputDatasetUuid, IoType.OUTPUT);
  }

  /**
   * Used to upsert an input or output dataset to a given job version.
   *
   * @param jobVersionUuid The unique ID of the job version.
   * @param datasetUuid The unique ID of the output dataset
   * @param ioType The {@link IoType} of the dataset.
   */
  @SqlUpdate(
      "INSERT INTO job_versions_io_mapping ("
          + "job_version_uuid, dataset_uuid, io_type) "
          + "VALUES (:jobVersionUuid, :datasetUuid, :ioType) ON CONFLICT DO NOTHING")
  void upsertInputOrOutputDatasetFor(UUID jobVersionUuid, UUID datasetUuid, IoType ioType);

  /**
   * Returns the input datasets to a given job version.
   *
   * @param jobVersionUuid The unique ID of the job version.
   * @return The input datasets for the job version.
   */
  default List<UUID> findInputDatasetsFor(UUID jobVersionUuid) {
    return findInputOrOutputDatasetsFor(jobVersionUuid, IoType.INPUT);
  }

  /**
   * Returns the output datasets to a given job version.
   *
   * @param jobVersionUuid The unique ID of the job version.
   * @return The output datasets for the job version.
   */
  default List<UUID> findOutputDatasetsFor(UUID jobVersionUuid) {
    return findInputOrOutputDatasetsFor(jobVersionUuid, IoType.OUTPUT);
  }

  /**
   * Returns the input or output datasets for a given job version.
   *
   * @param jobVersionUuid The unique ID of the job version.
   * @param ioType The {@link IoType} of the dataset.
   */
  @SqlQuery(
      "SELECT dataset_uuid FROM job_versions_io_mapping "
          + "WHERE job_version_uuid = :jobVersionUuid AND io_type = :ioType")
  List<UUID> findInputOrOutputDatasetsFor(UUID jobVersionUuid, IoType ioType);

  /**
   * Used to associate a {@link Run} to a given job version. A run is an instance of a job version.
   * When a run object is instantiated, the {@code latest_run_uuid} column in the {@code
   * job_versions} table is updated and set to the unique ID of the latest run of the version. Note,
   * multiple run instances may be linked to a job version as runs are based on a version.
   *
   * @param jobVersionUuid The unique ID of the job version.
   * @param updatedAt The last modified timestamp of the job version.
   * @param latestRunUuid The unique ID of the {@link Run} associated with the job version.
   */
  @SqlUpdate(
      "UPDATE job_versions "
          + "SET updated_at = :updatedAt, "
          + "    latest_run_uuid = :latestRunUuid "
          + "WHERE uuid = :jobVersionUuid")
  void updateLatestRunFor(UUID jobVersionUuid, Instant updatedAt, UUID latestRunUuid);

  /** Returns the unique ID of the latest {@link Run} for a given job version. */
  @SqlQuery("SELECT latest_run_uuid FROM job_versions WHERE uuid = :jobVersionUuid")
  Optional<UUID> findLatestRunFor(UUID jobVersionUuid);

  /** Returns the {@link JobVersionRow} object for a given the unique run ID . */
  @SqlQuery("SELECT * FROM job_versions WHERE latest_run_uuid = :runUuid")
  Optional<ExtendedJobVersionRow> findJobVersionFor(UUID runUuid);

  /** Returns the total row count for the {@code job_versions} table; used for testing only. */
  @VisibleForTesting
  @SqlQuery("SELECT COUNT(*) FROM job_versions")
  int count();

  /**
   * Used to upsert an immutable {@link JobVersionRow} object when a {@link Run} has transitioned. A
   * {@link Version} is generated using {@link Utils#newJobVersionFor(NamespaceName, JobName,
   * ImmutableSet, ImmutableSet, ImmutableMap, String)} based on the jobs inputs and inputs, source
   * code location, and context. A version for a given job is created <i>only</i> when a {@link Run}
   * transitions into a {@code COMPLETED}, {@code ABORTED}, or {@code FAILED} state.
   *
   * @param jobRow The job.
   * @param runUuid The unique ID of the run associated with the job version.
   * @param runState The current run state.
   * @param transitionedAt The timestamp of the run state transition.
   * @return A {@link BagOfJobVersionInfo} object.
   */
  default BagOfJobVersionInfo upsertJobVersionOnRunTransition(
      @NonNull JobRow jobRow,
      @NonNull UUID runUuid,
      @NonNull RunState runState,
      @NonNull Instant transitionedAt) {
    // Get the job.
    final JobDao jobDao = createJobDao();

    // Get the job context.
    final UUID jobContextUuid = jobRow.getJobContextUuid().get();
    final JobContextRow jobContextRow =
        createJobContextDao().findContextByUuid(jobContextUuid).get();
    final ImmutableMap<String, String> jobContext =
        Utils.fromJson(jobContextRow.getContext(), new TypeReference<>() {});

    // Get the inputs and outputs dataset versions for the run associated with the job version.
    final DatasetVersionDao datasetVersionDao = createDatasetVersionDao();
    final List<ExtendedDatasetVersionRow> jobVersionInputs =
        datasetVersionDao.findInputDatasetVersionsFor(runUuid);
    final List<ExtendedDatasetVersionRow> jobVersionOutputs =
        datasetVersionDao.findOutputDatasetVersionsFor(runUuid);

    // Get the namespace for the job.
    final NamespaceRow namespaceRow =
        createNamespaceDao().findNamespaceByName(jobRow.getNamespaceName()).get();

    // Generate the version for the job; the version may already exist.
    final Version jobVersion =
        Utils.newJobVersionFor(
            NamespaceName.of(jobRow.getNamespaceName()),
            JobName.of(
                Optional.ofNullable(jobRow.getParentJobName())
                    .map(pn -> pn + "." + jobRow.getSimpleName())
                    .orElse(jobRow.getName())),
            toDatasetIds(jobVersionInputs),
            toDatasetIds(jobVersionOutputs),
            jobContext,
            jobRow.getLocation());

    // Add the job version.
    final JobVersionDao jobVersionDao = createJobVersionDao();
    final JobVersionRow jobVersionRow =
        jobVersionDao.upsertJobVersion(
            UUID.randomUUID(),
            transitionedAt, // Use the timestamp of when the run state transitioned.
            jobRow.getUuid(),
            jobContextUuid,
            jobRow.getLocation(),
            jobVersion.getValue(),
            jobRow.getName(),
            namespaceRow.getUuid(),
            jobRow.getNamespaceName());

    // Link the input datasets to the job version.
    jobVersionInputs.forEach(
        jobVersionInput -> {
          jobVersionDao.upsertInputDatasetFor(
              jobVersionRow.getUuid(), jobVersionInput.getDatasetUuid());
        });

    // Link the output datasets to the job version.
    jobVersionOutputs.forEach(
        jobVersionOutput -> {
          jobVersionDao.upsertOutputDatasetFor(
              jobVersionRow.getUuid(), jobVersionOutput.getDatasetUuid());
        });

    // Link the job version to the run.
    createRunDao().updateJobVersion(runUuid, jobVersionRow.getUuid());

    // Link the run to the job version; multiple run instances may be linked to a job version.
    jobVersionDao.updateLatestRunFor(jobVersionRow.getUuid(), transitionedAt, runUuid);

    // Link the job version to the job only if the run is marked done and has transitioned into one
    // of the following states: COMPLETED, ABORTED, or FAILED.
    if (runState.isDone()) {
      jobDao.updateVersionFor(jobRow.getUuid(), transitionedAt, jobVersionRow.getUuid());
    }

    return new BagOfJobVersionInfo(jobRow, jobVersionRow, jobVersionInputs, jobVersionOutputs);
  }

  /** Returns the specified {@link ExtendedDatasetVersionRow}s as {@link DatasetId}s. */
  default ImmutableSortedSet<DatasetId> toDatasetIds(
      @NonNull final List<ExtendedDatasetVersionRow> datasetVersionRows) {
    final ImmutableSortedSet.Builder<DatasetId> datasetIds = ImmutableSortedSet.naturalOrder();
    for (final ExtendedDatasetVersionRow datasetVersionRow : datasetVersionRows) {
      datasetIds.add(
          new DatasetId(
              NamespaceName.of(datasetVersionRow.getNamespaceName()),
              DatasetName.of(datasetVersionRow.getDatasetName())));
    }
    return datasetIds.build();
  }

  /** A container class for job version info. */
  @Value
  class BagOfJobVersionInfo {
    JobRow jobRow;
    JobVersionRow jobVersionRow;
    List<ExtendedDatasetVersionRow> inputs;
    List<ExtendedDatasetVersionRow> outputs;
  }
}
