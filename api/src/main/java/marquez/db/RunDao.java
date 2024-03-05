/*
 * Copyright 2018-2023 contributors to the Marquez project
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
import lombok.Builder;
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
          + "WHERE uuid = :rowUuid")
  void updateStartState(UUID rowUuid, Instant transitionedAt, UUID startRunStateUuid);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :transitionedAt, "
          + "    end_run_state_uuid = :endRunStateUuid, "
          + "    ended_at = :transitionedAt "
          + "WHERE uuid = :rowUuid")
  void updateEndState(UUID rowUuid, Instant transitionedAt, UUID endRunStateUuid);

  String BASE_FIND_RUN_SQL =
      """
      SELECT r.*, ra.args, f.facets,
      jv.version AS job_version,
      ri.input_versions, ro.output_versions, df.dataset_facets
      FROM runs_view AS r
      LEFT OUTER JOIN
      (
          SELECT rf.run_uuid, JSON_AGG(rf.facet ORDER BY rf.lineage_event_time ASC) AS facets
          FROM run_facets_view rf
          GROUP BY rf.run_uuid
      ) AS f ON r.uuid=f.run_uuid
      LEFT OUTER JOIN run_args AS ra ON ra.uuid = r.run_args_uuid
      LEFT OUTER JOIN job_versions jv ON jv.uuid=r.job_version_uuid
      LEFT OUTER JOIN (
          SELECT im.run_uuid, JSON_AGG(json_build_object('namespace', dv.namespace_name,
              'name', dv.dataset_name,
              'version', dv.version,
              'dataset_version_uuid', uuid)) AS input_versions
          FROM runs_input_mapping im
          INNER JOIN dataset_versions dv on im.dataset_version_uuid = dv.uuid
          GROUP BY im.run_uuid
      ) ri ON ri.run_uuid=r.uuid
      LEFT OUTER JOIN (
          SELECT run_uuid, JSON_AGG(json_build_object('namespace', namespace_name,
                                                      'name', dataset_name,
                                                      'version', version,
                                                      'dataset_version_uuid', uuid
                                                      )) AS output_versions
          FROM dataset_versions
          GROUP BY run_uuid
      ) ro ON ro.run_uuid=r.uuid
      LEFT OUTER JOIN (
          SELECT
              run_uuid,
              JSON_AGG(json_build_object(
                  'dataset_version_uuid', dataset_version_uuid,
                  'name', name,
                  'type', type,
                  'facet', facet
              ) ORDER BY created_at ASC) as dataset_facets
          FROM dataset_facets_view
          WHERE (type ILIKE 'output' OR type ILIKE 'input')
          GROUP BY run_uuid
      ) AS df ON r.uuid = df.run_uuid
      """;

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
          WITH filtered_jobs AS (
            SELECT
                jv.uuid,
                jv.namespace_name,
                jv.name
            FROM jobs_view jv
            WHERE jv.namespace_name=:namespace AND (jv.name=:jobName OR :jobName = ANY(jv.aliases))
          ),
          run_facets_agg AS (
            SELECT
                run_uuid,
                JSON_AGG(facet ORDER BY lineage_event_time ASC) AS facets
            FROM run_facets_view
            -- This filter here is used for performance purpose: we only aggregate the json of run_uuid that matters
            WHERE
                run_uuid IN (SELECT uuid FROM runs_view WHERE job_uuid IN (SELECT uuid FROM filtered_jobs))
            GROUP BY run_uuid
          ),
          input_versions_agg AS (
               SELECT
                   im.run_uuid,
                   JSON_AGG(json_build_object('namespace', dv.namespace_name,
                        'name', dv.dataset_name,
                        'version', dv.version,
                        'dataset_version_uuid', dv.uuid
                   )) AS input_versions
               FROM runs_input_mapping im
               INNER JOIN dataset_versions dv ON im.dataset_version_uuid = dv.uuid
               -- This filter here is used for performance purpose: we only aggregate the json of run_uuid that matters
               WHERE
                   im.run_uuid IN (SELECT uuid FROM runs_view WHERE job_uuid IN (SELECT uuid FROM filtered_jobs))
               GROUP BY im.run_uuid
          ),
          output_versions_agg AS (
              SELECT
                  dv.run_uuid,
              JSON_AGG(json_build_object('namespace', namespace_name,
                                       'name', dataset_name,
                                       'version', version,
                                       'dataset_version_uuid', uuid
                                       )) AS output_versions
              FROM dataset_versions dv
              -- This filter here is used for performance purpose: we only aggregate the json of run_uuid that matters
              WHERE dv.run_uuid IN (SELECT uuid FROM runs_view WHERE job_uuid IN (SELECT uuid FROM filtered_jobs))
              GROUP BY dv.run_uuid
          ),
          dataset_facets_agg AS (
              SELECT
                  run_uuid,
                  JSON_AGG(json_build_object(
                      'dataset_version_uuid', dataset_version_uuid,
                      'name', name,
                      'type', type,
                      'facet', facet
                  ) ORDER BY created_at ASC) as dataset_facets
              FROM dataset_facets_view
              -- This filter here is used for performance purpose: we only aggregate the json of run_uuid that matters
              WHERE run_uuid IN (SELECT uuid FROM runs_view WHERE job_uuid IN (SELECT uuid FROM filtered_jobs))
              AND (type ILIKE 'output' OR type ILIKE 'input')
              GROUP BY run_uuid
          )
          SELECT
              r.*,
              ra.args,
              f.facets,
              jv.version AS job_version,
              ri.input_versions,
              ro.output_versions,
              df.dataset_facets
          FROM runs_view r
          INNER JOIN filtered_jobs fj ON r.job_uuid = fj.uuid
          LEFT JOIN run_facets_agg f ON r.uuid = f.run_uuid
          LEFT JOIN run_args ra ON ra.uuid = r.run_args_uuid
          LEFT JOIN job_versions jv ON jv.uuid = r.job_version_uuid
          LEFT JOIN input_versions_agg ri ON r.uuid = ri.run_uuid
          LEFT JOIN output_versions_agg ro ON r.uuid = ro.run_uuid
          LEFT JOIN dataset_facets_agg df ON r.uuid = df.run_uuid
          ORDER BY r.started_at DESC NULLS LAST
          LIMIT :limit OFFSET :offset
      """)
  List<Run> findAll(String namespace, String jobName, int limit, int offset);

  @SqlQuery("SELECT count(*) FROM runs_view AS r WHERE r.job_name = :jobName")
  int countFor(String jobName);

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
          + "location "
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
          + ":location "
          + ") ON CONFLICT(uuid) DO "
          + "UPDATE SET "
          + "external_id = EXCLUDED.external_id, "
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
      String location);

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
          + "location "
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
          + ":location "
          + ") ON CONFLICT(uuid) DO "
          + "UPDATE SET "
          + "external_id = EXCLUDED.external_id, "
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
      String namespaceName,
      String jobName,
      String location);

  default RunRow upsert(RunUpsert runUpsert) {
    if (runUpsert.runStateType == null) {
      return upsert(
          runUpsert.runUuid(),
          runUpsert.parentRunUuid(),
          runUpsert.externalId(),
          runUpsert.now(),
          runUpsert.jobUuid(),
          runUpsert.jobVersionUuid(),
          runUpsert.runArgsUuid(),
          runUpsert.nominalStartTime(),
          runUpsert.nominalEndTime(),
          runUpsert.namespaceName(),
          runUpsert.jobName(),
          runUpsert.location());
    } else {
      return upsert(
          runUpsert.runUuid(),
          runUpsert.parentRunUuid(),
          runUpsert.externalId(),
          runUpsert.now(),
          runUpsert.jobUuid(),
          runUpsert.jobVersionUuid(),
          runUpsert.runArgsUuid(),
          runUpsert.nominalStartTime(),
          runUpsert.nominalEndTime(),
          runUpsert.runStateType(),
          runUpsert.runStateTime(),
          runUpsert.namespaceName(),
          runUpsert.jobName(),
          runUpsert.location());
    }
  }

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
            jobRow.getLocation());

    updateInputDatasetMapping(jobRow.getInputs(), uuid);

    createRunStateDao().updateRunStateFor(uuid, currentState, now);

    return runRow;
  }

  @SqlUpdate("UPDATE runs SET job_version_uuid = :jobVersionUuid WHERE uuid = :runUuid")
  void updateJobVersion(UUID runUuid, UUID jobVersionUuid);

  @SqlQuery(
      BASE_FIND_RUN_SQL
          + """
      WHERE r.uuid=(
        SELECT r.uuid FROM runs_view r
        INNER JOIN jobs_view j ON j.namespace_name=r.namespace_name AND j.name=r.job_name
        WHERE j.namespace_name=:namespace AND (j.name=:jobName OR j.name=ANY(j.aliases))
        ORDER BY transitioned_at DESC
        LIMIT 1
      )
      """)
  Optional<Run> findByLatestJob(String namespace, String jobName);

  @Builder
  record RunUpsert(
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
      String location) {}
}
