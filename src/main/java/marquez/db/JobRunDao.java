package marquez.db;

import java.util.UUID;
import marquez.db.mappers.JobRunRowMapper;
import marquez.service.models.JobRun;
import marquez.service.models.RunArgs;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
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
      "INSERT INTO job_runs (guid, job_version_guid, current_state, "
          + " job_run_args_hex_digest, nominal_start_time, nominal_end_time) "
          + "VALUES (:guid, :jobVersionGuid, :currentState, :runArgsHexDigest, "
          + " :nominalStartTime, :nominalEndTime)")
  void insertJobRun(@BindBean JobRun jobRun);

  @Transaction
  default void insert(JobRun jobRun) {
    insertJobRun(jobRun);
    createJobRunStateDao().insert(UUID.randomUUID(), jobRun.getGuid(), jobRun.getCurrentState());
  }

  @Transaction
  default void insertJobRunAndArgs(JobRun jobRun, RunArgs runArgs) {
    createJobRunArgsDao().insert(runArgs);
    insert(jobRun);
  }

  @SqlUpdate("UPDATE job_runs SET current_state = :state WHERE guid = :job_run_id")
  void updateCurrentState(@Bind("job_run_id") UUID jobRunID, @Bind("state") Integer state);

  @Transaction
  default void updateState(UUID jobRunID, Integer state) {
    updateCurrentState(jobRunID, state);
    createJobRunStateDao().insert(UUID.randomUUID(), jobRunID, state);
  }

  @SqlQuery(
      "SELECT jr.*, jra.args_json "
          + "FROM job_runs jr "
          + "LEFT JOIN job_run_args jra "
          + " ON (jr.guid = :guid AND jr.job_run_args_hex_digest = jra.hex_digest) "
          + "WHERE jr.guid = :guid")
  JobRun findJobRunById(@Bind("guid") UUID guid);
}
