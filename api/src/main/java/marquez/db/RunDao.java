/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.OpenLineageDao.DEFAULT_NAMESPACE_OWNER;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.db.mappers.ExtendedRunRowMapper;
import marquez.db.mappers.JobRowMapper;
import marquez.db.mappers.RunMapper;
import marquez.db.mappers.RunRowMapper;
import marquez.db.models.DatasetRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.service.models.Dataset;
import marquez.service.models.JobMeta;
import marquez.service.models.LineageEvent.SchemaField;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(ExtendedRunRowMapper.class)
@RegisterRowMapper(RunRowMapper.class)
@RegisterRowMapper(RunMapper.class)
@RegisterRowMapper(JobRowMapper.class)
public interface RunDao extends BaseDao {
  @SqlQuery("SELECT EXISTS (SELECT 1 FROM runs WHERE uuid = :rowUuid)")
  boolean exists(UUID rowUuid);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :transitionedAt, "
          + "    current_run_state = :currentRunState, "
          + "    transitioned_at = :transitionedAt "
          + "WHERE uuid = :rowUuid")
  void updateRunState(UUID rowUuid, Instant transitionedAt, RunState currentRunState);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :transitionedAt, "
          + "    start_run_state_uuid = :startRunStateUuid,"
          + "    started_at = :transitionedAt "
          + "WHERE uuid = :rowUuid AND (updated_at < :transitionedAt or start_run_state_uuid is null)")
  void updateStartState(UUID rowUuid, Instant transitionedAt, UUID startRunStateUuid);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :transitionedAt, "
          + "    end_run_state_uuid = :endRunStateUuid, "
          + "    ended_at = :transitionedAt "
          + "WHERE uuid = :rowUuid")
  void updateEndState(UUID rowUuid, Instant transitionedAt, UUID endRunStateUuid);

  String BASE_FIND_RUN_SQL =
      "SELECT r.*, ra.args, ctx.context, f.facets,\n"
          + "jv.version AS job_version,\n"
          + "ri.input_versions, ro.output_versions\n"
          + "FROM runs_view AS r\n"
          + "LEFT OUTER JOIN\n"
          + "(\n"
          + "    SELECT le.run_uuid, JSON_AGG(event->'run'->'facets') AS facets\n"
          + "    FROM lineage_events le\n"
          + "    GROUP BY le.run_uuid\n"
          + ") AS f ON r.uuid=f.run_uuid\n"
          + "LEFT OUTER JOIN run_args AS ra ON ra.uuid = r.run_args_uuid\n"
          + "LEFT OUTER JOIN job_contexts AS ctx ON r.job_context_uuid = ctx.uuid\n"
          + "LEFT OUTER JOIN job_versions jv ON jv.uuid=r.job_version_uuid\n"
          + "LEFT OUTER JOIN (\n"
          + " SELECT im.run_uuid, JSON_AGG(json_build_object('namespace', dv.namespace_name,\n"
          + "        'name', dv.dataset_name,\n"
          + "        'version', dv.version)) AS input_versions\n"
          + " FROM runs_input_mapping im\n"
          + " INNER JOIN dataset_versions dv on im.dataset_version_uuid = dv.uuid\n"
          + " GROUP BY im.run_uuid\n"
          + ") ri ON ri.run_uuid=r.uuid\n"
          + "LEFT OUTER JOIN (\n"
          + "  SELECT run_uuid, JSON_AGG(json_build_object('namespace', namespace_name,\n"
          + "                                              'name', dataset_name,\n"
          + "                                              'version', version)) AS output_versions\n"
          + "  FROM dataset_versions\n"
          + "  GROUP BY run_uuid\n"
          + ") ro ON ro.run_uuid=r.uuid\n";

  @SqlQuery(BASE_FIND_RUN_SQL + "WHERE r.uuid = :runUuid")
  Optional<Run> findRunByUuid(UUID runUuid);

  @SqlQuery(BASE_FIND_RUN_SQL + "WHERE r.uuid = :runUuid")
  Optional<ExtendedRunRow> findRunByUuidAsExtendedRow(UUID runUuid);

  @SqlQuery("SELECT * FROM runs r WHERE r.uuid = :runUuid")
  Optional<RunRow> findRunByUuidAsRow(UUID runUuid);

  @SqlQuery(
      """
  SELECT j.* FROM jobs_view j
  INNER JOIN runs_view r  ON r.job_uuid=j.uuid
  WHERE r.uuid=:uuid
""")
  Optional<JobRow> findJobRowByRunUuid(UUID uuid);

  @SqlQuery(
      """
          SELECT r.*, ra.args, ctx.context, f.facets,
          j.namespace_name, j.name, jv.version AS job_version,
          ri.input_versions, ro.output_versions
          FROM runs_view AS r
          INNER JOIN jobs_view j ON r.job_uuid=j.uuid
          LEFT JOIN LATERAL
          (
            SELECT le.run_uuid, JSON_AGG(event->'run'->'facets') AS facets
            FROM lineage_events le
            WHERE le.run_uuid=r.uuid
            GROUP BY le.run_uuid
          ) AS f ON r.uuid=f.run_uuid
          LEFT OUTER JOIN run_args AS ra ON ra.uuid = r.run_args_uuid
          LEFT OUTER JOIN job_contexts AS ctx ON r.job_context_uuid = ctx.uuid
          LEFT OUTER JOIN job_versions jv ON jv.uuid=r.job_version_uuid
          LEFT OUTER JOIN (
           SELECT im.run_uuid, JSON_AGG(json_build_object('namespace', dv.namespace_name,
                  'name', dv.dataset_name,
                  'version', dv.version)) AS input_versions
           FROM runs_input_mapping im
           INNER JOIN dataset_versions dv on im.dataset_version_uuid = dv.uuid
           GROUP BY im.run_uuid
          ) ri ON ri.run_uuid=r.uuid
          LEFT OUTER JOIN (
            SELECT run_uuid, JSON_AGG(json_build_object('namespace', namespace_name,
                                                        'name', dataset_name,
                                                        'version', version)) AS output_versions
            FROM dataset_versions
            GROUP BY run_uuid
          ) ro ON ro.run_uuid=r.uuid
          WHERE j.namespace_name=:namespace AND (j.name=:jobName OR :jobName = ANY(j.aliases))
          ORDER BY STARTED_AT DESC NULLS LAST
          LIMIT :limit OFFSET :offset
      """)
  List<Run> findAll(String namespace, String jobName, int limit, int offset);

  @SqlQuery(
      "INSERT INTO runs ( "
          + "uuid, "
          + "parent_run_uuid, "
          + "external_id, "
          + "created_at, "
          + "updated_at, "
          + "job_uuid, "
          + "job_version_uuid, "
          + "run_args_uuid, "
          + "nominal_start_time, "
          + "nominal_end_time,"
          + "current_run_state, "
          + "transitioned_at, "
          + "namespace_name, "
          + "job_name, "
          + "location, "
          + "job_context_uuid "
          + ") VALUES ( "
          + ":runUuid, "
          + ":parentRunUuid, "
          + ":externalId, "
          + ":now, "
          + ":now, "
          + ":jobUuid,"
          + ":jobVersionUuid, "
          + ":runArgsUuid, "
          + ":nominalStartTime, "
          + ":nominalEndTime, "
          + ":runStateType,"
          + ":runStateTime, "
          + ":namespaceName, "
          + ":jobName, "
          + ":location, "
          + ":jobContextUuid "
          + ") ON CONFLICT(uuid) DO "
          + "UPDATE SET "
          + "updated_at = EXCLUDED.updated_at, "
          + "current_run_state = EXCLUDED.current_run_state, "
          + "transitioned_at = EXCLUDED.transitioned_at, "
          + "nominal_start_time = COALESCE(EXCLUDED.nominal_start_time, runs.nominal_start_time), "
          + "nominal_end_time = COALESCE(EXCLUDED.nominal_end_time, runs.nominal_end_time), "
          + "location = EXCLUDED.location "
          + "RETURNING *")
  RunRow upsert(
      UUID runUuid,
      UUID parentRunUuid,
      String externalId,
      Instant now,
      UUID jobUuid,
      UUID jobVersionUuid,
      UUID runArgsUuid,
      Instant nominalStartTime,
      Instant nominalEndTime,
      RunState runStateType,
      Instant runStateTime,
      String namespaceName,
      String jobName,
      String location,
      UUID jobContextUuid);

  @SqlQuery(
      "INSERT INTO runs ( "
          + "uuid, "
          + "parent_run_uuid, "
          + "external_id, "
          + "created_at, "
          + "updated_at, "
          + "job_uuid, "
          + "job_version_uuid, "
          + "run_args_uuid, "
          + "nominal_start_time, "
          + "nominal_end_time, "
          + "namespace_name, "
          + "job_name, "
          + "location, "
          + "job_context_uuid "
          + ") VALUES ( "
          + ":runUuid, "
          + ":parentRunUuid, "
          + ":externalId, "
          + ":now, "
          + ":now, "
          + ":jobUuid, "
          + ":jobVersionUuid, "
          + ":runArgsUuid, "
          + ":nominalStartTime, "
          + ":nominalEndTime, "
          + ":namespaceName, "
          + ":jobName, "
          + ":location, "
          + ":jobContextUuid "
          + ") ON CONFLICT(uuid) DO "
          + "UPDATE SET "
          + "updated_at = EXCLUDED.updated_at, "
          + "nominal_start_time = COALESCE(EXCLUDED.nominal_start_time, runs.nominal_start_time), "
          + "nominal_end_time = COALESCE(EXCLUDED.nominal_end_time, runs.nominal_end_time), "
          + "location = EXCLUDED.location "
          + "RETURNING *")
  RunRow upsert(
      UUID runUuid,
      UUID parentRunUuid,
      String externalId,
      Instant now,
      UUID jobUuid,
      UUID jobVersionUuid,
      UUID runArgsUuid,
      Instant nominalStartTime,
      Instant nominalEndTime,
      UUID namespaceUuid,
      String namespaceName,
      String jobName,
      String location,
      UUID jobContextUuid);

  @SqlUpdate(
      "INSERT INTO runs_input_mapping (run_uuid, dataset_version_uuid) "
          + "VALUES (:runUuid, :datasetVersionUuid) ON CONFLICT DO NOTHING")
  void updateInputMapping(UUID runUuid, UUID datasetVersionUuid);

  @Transaction
  default void notifyJobChange(UUID runUuid, JobRow jobRow, JobMeta jobMeta) {
    upsertRun(runUuid, jobRow.getName(), jobRow.getNamespaceName());

    updateInputDatasetMapping(jobMeta.getInputs(), runUuid);

    upsertOutputDatasetsFor(runUuid, jobMeta.getOutputs());
  }

  default void upsertOutputDatasetsFor(UUID runUuid, ImmutableSet<DatasetId> runOutputIds) {
    DatasetVersionDao datasetVersionDao = createDatasetVersionDao();
    DatasetDao datasetDao = createDatasetDao();
    OpenLineageDao openLineageDao = createOpenLineageDao();

    if (runOutputIds != null) {
      for (DatasetId runOutputId : runOutputIds) {
        Optional<DatasetRow> dsRow =
            datasetDao.findDatasetAsRow(
                runOutputId.getNamespace().getValue(), runOutputId.getName().getValue());
        Optional<Dataset> ds =
            datasetDao.findDatasetByName(
                runOutputId.getNamespace().getValue(), runOutputId.getName().getValue());
        ds.ifPresent(
            d -> {
              UUID version =
                  Utils.newDatasetVersionFor(
                          d.getNamespace().getValue(),
                          d.getSourceName().getValue(),
                          d.getPhysicalName().getValue(),
                          d.getName().getValue(),
                          null,
                          toSchemaFields(d.getFields()),
                          runUuid)
                      .getValue();
              datasetVersionDao.upsert(
                  UUID.randomUUID(),
                  Instant.now(),
                  dsRow.get().getUuid(),
                  version,
                  runUuid,
                  datasetVersionDao.toPgObjectFields(d.getFields()),
                  d.getNamespace().getValue(),
                  d.getName().getValue(),
                  null);
            });
      }
    }
  }

  default List<SchemaField> toSchemaFields(List<Field> fields) {
    if (fields == null) {
      return null;
    }
    return fields.stream()
        .map(
            f ->
                SchemaField.builder()
                    .name(f.getName().getValue())
                    .type(f.getType())
                    .description(f.getDescription().orElse(null))
                    .build())
        .collect(Collectors.toList());
  }

  default void updateInputDatasetMapping(Set<DatasetId> inputs, UUID runUuid) {
    if (inputs == null) {
      return;
    }
    DatasetDao datasetDao = createDatasetDao();

    for (DatasetId datasetId : inputs) {
      Optional<Dataset> dataset =
          datasetDao.findDatasetByName(
              datasetId.getNamespace().getValue(), datasetId.getName().getValue());
      if (dataset.isPresent() && dataset.get().getCurrentVersion().isPresent()) {
        updateInputMapping(runUuid, dataset.get().getCurrentVersion().get());
      }
    }
  }

  @SqlUpdate(
      "UPDATE runs SET job_name = :jobName, "
          + "namespace_name = :namespaceName "
          + "WHERE uuid = :runUuid")
  void upsertRun(UUID runUuid, @NonNull String jobName, @NonNull String namespaceName);

  /** Insert from run creates a run but does not associate any datasets. */
  @Transaction
  default RunRow upsertRunMeta(
      NamespaceName namespaceName, JobRow jobRow, RunMeta runMeta, RunState currentState) {
    Instant now = Instant.now();

    NamespaceRow namespaceRow =
        createNamespaceDao()
            .upsertNamespaceRow(
                UUID.randomUUID(), now, namespaceName.getValue(), DEFAULT_NAMESPACE_OWNER);

    RunArgsRow runArgsRow =
        createRunArgsDao()
            .upsertRunArgs(
                UUID.randomUUID(),
                now,
                Utils.toJson(runMeta.getArgs()),
                Utils.checksumFor(runMeta.getArgs()));

    UUID uuid = runMeta.getId().map(RunId::getValue).orElse(UUID.randomUUID());

    RunRow runRow =
        upsert(
            uuid,
            null,
            null,
            now,
            jobRow.getUuid(),
            null,
            runArgsRow.getUuid(),
            runMeta.getNominalStartTime().orElse(null),
            runMeta.getNominalEndTime().orElse(null),
            currentState,
            now,
            namespaceRow.getName(),
            jobRow.getName(),
            jobRow.getLocation(),
            jobRow.getJobContextUuid().orElse(null));

    updateInputDatasetMapping(jobRow.getInputs(), uuid);

    createRunStateDao().updateRunStateFor(uuid, currentState, now);

    return runRow;
  }

  @SqlUpdate("UPDATE runs SET job_version_uuid = :jobVersionUuid WHERE uuid = :runUuid")
  void updateJobVersion(UUID runUuid, UUID jobVersionUuid);

  @SqlQuery(
      """
      WITH RECURSIVE job_names AS (
        SELECT uuid, namespace_name, name, symlink_target_uuid
        FROM jobs_view j
        WHERE j.namespace_name=:namespace AND j.name=:jobName
        UNION
        SELECT j.uuid, j.namespace_name, j.name, j.symlink_target_uuid
        FROM jobs_view j
        INNER JOIN job_names jn ON j.uuid=jn.symlink_target_uuid OR j.symlink_target_uuid=jn.uuid
      )
      """
          + BASE_FIND_RUN_SQL
          + """
      WHERE r.uuid=(
        SELECT r.uuid FROM runs_view r
        INNER JOIN job_names j ON j.namespace_name=r.namespace_name AND j.name=r.job_name
        ORDER BY transitioned_at DESC
        LIMIT 1
      )
      """)
  Optional<Run> findByLatestJob(String namespace, String jobName);
}
