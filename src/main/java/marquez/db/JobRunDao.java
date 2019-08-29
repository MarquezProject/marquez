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

import java.util.List;
import java.util.UUID;
import marquez.db.mappers.JobRunRowMapper;
import marquez.service.models.JobRun;
import marquez.service.models.RunArgs;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(JobRunRowMapper.class)
public interface JobRunDao {
  @CreateSqlObject
  JobRunStateDao createJobRunStateDao();

  @CreateSqlObject
  JobRunArgsDao createJobRunArgsDao();

  @SqlUpdate(
      "INSERT INTO job_runs (uuid, job_version_uuid, current_state, "
          + " job_run_args_hex_digest, nominal_start_time, nominal_end_time) "
          + "VALUES (:uuid, :jobVersionUuid, :currentState, :runArgsHexDigest, "
          + " :nominalStartTime, :nominalEndTime)")
  void insertJobRun(@BindBean JobRun jobRun);

  @Transaction
  default void insert(JobRun jobRun) {
    insertJobRun(jobRun);
    createJobRunStateDao().insert(UUID.randomUUID(), jobRun.getUuid(), jobRun.getCurrentState());
  }

  @Transaction
  default void insertJobRunAndArgs(JobRun jobRun, RunArgs runArgs) {
    createJobRunArgsDao().insert(runArgs);
    insert(jobRun);
  }

  @SqlUpdate("UPDATE job_runs SET current_state = :state WHERE uuid = :jobRunUuid")
  void updateCurrentState(UUID jobRunUuid, Integer state);

  @Transaction
  default void updateState(UUID jobRunUuid, Integer state) {
    updateCurrentState(jobRunUuid, state);
    createJobRunStateDao().insert(UUID.randomUUID(), jobRunUuid, state);
  }

  @SqlQuery(
      "SELECT jr.*, jra.args_json "
          + "FROM job_runs jr "
          + "LEFT JOIN job_run_args jra "
          + " ON (jr.uuid = :uuid AND jr.job_run_args_hex_digest = jra.hex_digest) "
          + "WHERE jr.uuid = :uuid")
  JobRun findJobRunById(UUID uuid);

  @SqlQuery(
      "SELECT jr.*, jra.args_json "
          + "FROM job_runs jr "
          + "INNER JOIN job_versions jv "
          + " ON (jr.job_version_uuid = jv.uuid AND jv.job_uuid = :jobUuid)"
          + "LEFT JOIN job_run_args jra "
          + " ON (jr.job_run_args_hex_digest = jra.hex_digest) "
          + "ORDER BY created_at DESC "
          + "LIMIT :limit OFFSET :offset")
  List<JobRun> findAllByJobUuid(UUID jobUuid, int limit, int offset);
}
