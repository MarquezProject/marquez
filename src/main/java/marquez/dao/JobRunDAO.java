package marquez.dao;

import java.util.UUID;
import marquez.core.models.JobRun;
import marquez.core.models.RunArgs;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterRowMapper(JobRunRow.class)
public interface JobRunDAO extends SqlObject {
  Logger LOG = LoggerFactory.getLogger(JobRunDAO.class);

  @CreateSqlObject
  JobRunStateDAO createJobRunStateDAO();

  @CreateSqlObject
  RunArgsDAO createRunArgsDAO();

  String insertJobRunSQL =
      "INSERT INTO job_runs (guid, job_version_guid, current_state, job_run_args_hex_digest, nominal_start_time, nominal_end_time) "
          + "VALUES (:guid, :jobVersionGuid, :currentState, :runArgsHexDigest, :nominalStartTime, :nominalEndTime)";

  @SqlUpdate(insertJobRunSQL)
  void insertJobRun(@BindBean JobRun jobRun);

  @Transaction
  default void insert(JobRun jobRun) {
    insertJobRun(jobRun);
    createJobRunStateDAO().insert(UUID.randomUUID(), jobRun.getGuid(), jobRun.getCurrentState());
  }

  @Transaction
  default void insertJobRunAndArgs(JobRun jobRun, RunArgs runArgs) {
    createRunArgsDAO().insert(runArgs);
    insert(jobRun);
  }

  String updateStateSQL = "UPDATE job_runs SET current_state = :state WHERE guid = :job_run_id";

  @SqlUpdate(updateStateSQL)
  void updateCurrentState(@Bind("job_run_id") UUID jobRunID, @Bind("state") Integer state);

  @Transaction
  default void updateState(UUID jobRunID, Integer state) {
    updateCurrentState(jobRunID, state);
    createJobRunStateDAO().insert(UUID.randomUUID(), jobRunID, state);
  }

  @SqlQuery(
      "SELECT jr.*, jra.args_json FROM job_runs jr LEFT JOIN job_run_args jra ON (jr.guid = :guid AND jr.job_run_args_hex_digest = jra.hex_digest)")
  JobRun findJobRunById(@Bind("guid") UUID guid);
}
