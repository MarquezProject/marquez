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

package marquez.db;

import static marquez.db.Columns.ENDED_AT;
import static marquez.db.Columns.STARTED_AT;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.RunState;
import marquez.db.mappers.ExtendedRunRowMapper;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.RunRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(ExtendedRunRowMapper.class)
public interface RunDao extends SqlObject {
  @CreateSqlObject
  JobVersionDao createJobVersionDao();

  @Transaction
  default void insertWith(RunRow row, List<UUID> inputVersionUuids) {
    insert(row);
  }

  @Transaction
  default void insert(RunRow row) {
    withHandle(
        handle ->
            handle
                .createUpdate(
                    "INSERT INTO runs ("
                        + "uuid, "
                        + "created_at, "
                        + "updated_at, "
                        + "job_version_uuid, "
                        + "run_args_uuid, "
                        + "nominal_start_time, "
                        + "nominal_end_time"
                        + ") VALUES ("
                        + ":uuid, "
                        + ":createdAt, "
                        + ":updatedAt, "
                        + ":jobVersionUuid, "
                        + ":runArgsUuid, "
                        + ":nominalStartTime, "
                        + ":nominalEndTime)")
                .bindBean(row)
                .execute());
    // Input versions
    row.getInputVersionUuids()
        .forEach(inputVersionUuid -> updateInputVersions(row.getUuid(), inputVersionUuid));
    // Latest run
    final Instant updateAt = row.getCreatedAt();
    createJobVersionDao().updateLatestRun(row.getJobVersionUuid(), updateAt, row.getUuid());
  }

  @Transaction
  default void updateJobVersionUuid(UUID rowUuid, Instant updatedAt, UUID jobVersionUuid) {
    withHandle(
        handle ->
            handle
                .createUpdate(
                    "UPDATE runs "
                        + "SET updated_at = :updatedAt, "
                        + "    job_version_uuid = :jobVersionUuid "
                        + "WHERE uuid = :rowUuid")
                .bind("updatedAt", updatedAt)
                .bind("jobVersionUuid", jobVersionUuid)
                .bind("rowUuid", rowUuid)
                .execute());
    createJobVersionDao().updateLatestRun(jobVersionUuid, updatedAt, rowUuid);
  }

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM runs WHERE uuid = :rowUuid)")
  boolean exists(UUID rowUuid);

  @SqlUpdate(
      "INSERT INTO runs_input_mapping (run_uuid, dataset_version_uuid) "
          + "VALUES (:runUuid, :datasetVersionUuid) ON CONFLICT DO NOTHING")
  void updateInputVersions(UUID runUuid, UUID datasetVersionUuid);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :updatedAt, "
          + "    current_run_state = :currentRunState "
          + "WHERE uuid = :rowUuid")
  void updateRunState(UUID rowUuid, Instant updatedAt, String currentRunState);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :updatedAt, "
          + "    start_run_state_uuid = :startRunStateUuid "
          + "WHERE uuid = :rowUuid")
  void updateStartState(UUID rowUuid, Instant updatedAt, UUID startRunStateUuid);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :updatedAt, "
          + "    start_run_state_uuid = :startRunStateUuid "
          + "WHERE uuid = :rowUuid AND (updated_at < :updatedAt or start_run_state_uuid is null)")
  void upsertStartState(UUID rowUuid, Instant updatedAt, UUID startRunStateUuid);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :updatedAt, "
          + "    end_run_state_uuid = :endRunStateUuid "
          + "WHERE uuid = :rowUuid")
  void updateEndState(UUID rowUuid, Instant updatedAt, UUID endRunStateUuid);

  static final String SELECT_RUN =
      "SELECT r.*, ra.args, rs_s.transitioned_at as "
          + STARTED_AT
          + ", rs_e.transitioned_at as "
          + ENDED_AT
          + ", "
          + "ARRAY(SELECT dataset_version_uuid "
          + "      FROM runs_input_mapping "
          + "      WHERE run_uuid = r.uuid) AS input_version_uuids "
          + "FROM runs AS r "
          + "INNER JOIN run_args AS ra"
          + "  ON (ra.uuid = r.run_args_uuid) "
          + "LEFT JOIN run_states AS rs_s"
          + "  ON (rs_s.uuid = r.start_run_state_uuid) "
          + "LEFT JOIN run_states AS rs_e"
          + "  ON (rs_e.uuid = r.end_run_state_uuid) ";

  @SqlQuery(SELECT_RUN + " WHERE r.uuid = :rowUuid")
  Optional<ExtendedRunRow> findBy(UUID rowUuid);

  @SqlQuery(
      SELECT_RUN
          + "INNER JOIN job_versions AS jv ON r.job_version_uuid = jv.uuid "
          + "INNER JOIN jobs AS j ON jv.job_uuid = j.uuid "
          + "INNER JOIN namespaces AS n ON j.namespace_uuid = n.uuid "
          + "WHERE n.name = :namespace and j.name = :jobName "
          + "ORDER BY r.created_at DESC "
          + "LIMIT :limit OFFSET :offset")
  List<ExtendedRunRow> findAll(String namespace, String jobName, int limit, int offset);

  @SqlQuery("SELECT COUNT(*) FROM runs")
  int count();

  @SqlQuery(
      "INSERT INTO runs ( "
          + "uuid, "
          + "external_id, "
          + "created_at, "
          + "updated_at, "
          + "job_version_uuid, "
          + "run_args_uuid, "
          + "nominal_start_time, "
          + "nominal_end_time,"
          + "current_run_state "
          + ") VALUES ( "
          + ":runUuid, "
          + ":externalId, "
          + ":now, "
          + ":now, "
          + ":jobVersionUuid, "
          + ":runArgsUuid, "
          + ":nominalStartTime, "
          + ":nominalEndTime, "
          + ":runStateType "
          + ") ON CONFLICT(uuid) DO "
          + "UPDATE SET "
          + "updated_at = EXCLUDED.updated_at, "
          + "current_run_state = EXCLUDED.current_run_state, "
          + "nominal_start_time = EXCLUDED.nominal_start_time, "
          + "nominal_end_time = EXCLUDED.nominal_end_time "
          + "RETURNING *")
  ExtendedRunRow upsert(
      UUID runUuid,
      String externalId,
      Instant now,
      UUID jobVersionUuid,
      UUID runArgsUuid,
      Instant nominalStartTime,
      Instant nominalEndTime,
      RunState runStateType);

  @SqlQuery(
      "INSERT INTO runs ( "
          + "uuid, "
          + "external_id, "
          + "created_at, "
          + "updated_at, "
          + "job_version_uuid, "
          + "run_args_uuid, "
          + "nominal_start_time, "
          + "nominal_end_time"
          + ") VALUES ( "
          + ":runUuid, "
          + ":externalId, "
          + ":now, "
          + ":now, "
          + ":jobVersionUuid, "
          + ":runArgsUuid, "
          + ":nominalStartTime, "
          + ":nominalEndTime "
          + ") ON CONFLICT(uuid) DO "
          + "UPDATE SET "
          + "updated_at = EXCLUDED.updated_at, "
          + "current_run_state = EXCLUDED.current_run_state, "
          + "nominal_start_time = EXCLUDED.nominal_start_time "
          + "RETURNING *")
  ExtendedRunRow upsert(
      UUID runUuid,
      String externalId,
      Instant now,
      UUID jobVersionUuid,
      UUID runArgsUuid,
      Instant nominalStartTime,
      Instant nominalEndTime);

  @SqlUpdate(
      "INSERT INTO runs_input_mapping (run_uuid, dataset_version_uuid) "
          + "VALUES (:runUuid, :datasetVersionUuid) ON CONFLICT DO NOTHING")
  void updateInputMapping(UUID runUuid, UUID datasetVersionUuid);
}
