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

import com.github.henneberger.typekin.annotation.StructuralType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import marquez.common.MarquezModel;
import marquez.common.models.RunState;
import marquez.db.mappers.ExtendedRunRowMapper;
import marquez.db.mappers.RunMapper;
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
  @RegisterRowMapper(RunMapper.class)
  List<RunData> findAll(String namespace, String jobName, int limit, int offset);

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

  @StructuralType(model = MarquezModel.Run.class, name = "RunDataType")
  public static class RunData implements RunDataType {
    private final UUID uuid;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<DatasetVersionIdData> inputDatasetVersion;
    private final JobVersionIdData jobVersionId;
    private final Optional<Instant> nominalStartTime;
    private final Optional<Instant> nominalEndTime;
    private final Optional<CurrentRunStateData> currentRunState;
    private final Optional<RunStateData> startRunState;
    private final Optional<RunStateData> endRunState;
    private final RunArgsData runArgs;

    public RunData(
        UUID uuid,
        Instant createdAt,
        Instant updatedAt,
        JobVersionIdData jobVersionId,
        List<DatasetVersionIdData> inputDatasetVersion,
        Optional<Instant> nominalStartTime,
        Optional<Instant> nominalEndTime,
        Optional<CurrentRunStateData> currentRunState,
        Optional<RunStateData> startRunState,
        Optional<RunStateData> endRunState,
        RunArgsData runArgs) {
      this.uuid = uuid;
      this.createdAt = createdAt;
      this.updatedAt = updatedAt;
      this.inputDatasetVersion = inputDatasetVersion;
      this.jobVersionId = jobVersionId;
      this.nominalStartTime = nominalStartTime;
      this.nominalEndTime = nominalEndTime;
      this.currentRunState = currentRunState;
      this.startRunState = startRunState;
      this.endRunState = endRunState;
      this.runArgs = runArgs;
    }

    public UUID getUuid() {
      return uuid;
    }

    public Instant getCreatedAt() {
      return createdAt;
    }

    public Instant getUpdatedAt() {
      return updatedAt;
    }

    public List<DatasetVersionIdData> getInputDatasetVersion() {
      return inputDatasetVersion;
    }

    public JobVersionIdData getJobVersion() {
      return jobVersionId;
    }

    public Optional<Instant> getNominalStartTime() {
      return nominalStartTime;
    }

    public Optional<Instant> getNominalEndTime() {
      return nominalEndTime;
    }

    public Optional<CurrentRunStateData> getCurrentRunState() {
      return currentRunState;
    }

    public Optional<RunStateData> getStartRunState() {
      return startRunState;
    }

    public Optional<RunStateData> getEndRunState() {
      return endRunState;
    }

    public RunArgsData getRunArgs() {
      return runArgs;
    }
  }

  @StructuralType(model = MarquezModel.JobVersion.class, name = "JobVersionIdDataType")
  public static class JobVersionIdData implements JobVersionIdDataType {

    private final UUID uuid;

    public JobVersionIdData(UUID uuid) {

      this.uuid = uuid;
    }

    public UUID getUuid() {
      return uuid;
    }
  }

  @StructuralType(model = MarquezModel.RunState.class, name = "RunStateDataType")
  public static class RunStateData implements RunStateDataType {

    private final UUID uuid;
    private final Instant transitionTime;

    public RunStateData(UUID uuid, Instant transitionTime) {
      this.uuid = uuid;
      this.transitionTime = transitionTime;
    }

    public UUID getUuid() {
      return uuid;
    }

    public Instant getTransitionedAt() {
      return transitionTime;
    }
  }

  @StructuralType(model = MarquezModel.RunState.class, name = "CurrentRunStateDataType")
  public static class CurrentRunStateData implements CurrentRunStateDataType {

    private final RunState runState;

    public CurrentRunStateData(RunState runState) {
      this.runState = runState;
    }

    public marquez.common.models.RunState getState() {
      return runState;
    }
  }

  @StructuralType(model = MarquezModel.RunArgs.class, name = "RunArgsDataType")
  public static class RunArgsData implements RunArgsDataType {

    private final UUID uuid;
    private final Map<String, String> args;

    public RunArgsData(UUID uuid, Map<String, String> args) {
      this.uuid = uuid;
      this.args = args;
    }

    public UUID getUuid() {
      return uuid;
    }

    public Map<String, String> getArgs() {
      return args;
    }
  }

  @StructuralType(model = MarquezModel.DatasetVersion.class, name = "DatasetVersionDataType")
  public static class DatasetVersionIdData implements DatasetVersionDataType {

    private final UUID uuid;

    public DatasetVersionIdData(UUID uuid) {
      this.uuid = uuid;
    }

    public UUID getUuid() {
      return uuid;
    }
  }
}
