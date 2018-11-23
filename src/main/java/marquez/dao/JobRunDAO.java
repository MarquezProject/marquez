package marquez.dao;

import java.util.UUID;
import marquez.core.models.JobRun;
import marquez.core.models.JobRunState;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterRowMapper(JobRunRow.class)
public interface JobRunDAO extends SqlObject {
  Logger LOG = LoggerFactory.getLogger(JobRunDAO.class);

  @CreateSqlObject
  JobRunStateDAO createJobRunStateDAO();

  default void insert(JobRun jobRun) {
    try (final Handle handle = getHandle()) {
      handle.useTransaction(
          h -> {
            h.createUpdate(
                    "INSERT INTO job_runs (guid, started_at, job_version_guid, current_state, job_run_args_hex_digest) "
                        + "VALUES (:guid, :startedAt, :jobVersionGuid, :currentState, :runArgsHexDigest)")
                .bindBean(jobRun)
                .execute();
            createJobRunStateDAO()
                .insert(UUID.randomUUID(), jobRun.getGuid(), jobRun.getCurrentState());
          });
    } catch (Exception e) {
      // TODO: Add better error handling
      LOG.error(e.getMessage());
    }
  }

  default void update(JobRun jobRun) {
    try (final Handle handle = getHandle()) {
      handle.useTransaction(
          h -> {
            h.createUpdate(
                    "UPDATE job_runs SET current_state = :currentState, started_at = :startedAt, ended_at = :endedAt where guid = :guid")
                .bindBean(jobRun)
                .execute();
            createJobRunStateDAO()
                .insert(UUID.randomUUID(), jobRun.getGuid(), jobRun.getCurrentState());
          });
    } catch (Exception e) {
      // TODO: Add better error handling
      LOG.error(e.getMessage());
    }
  }
  
  String updateStateSQL = "UPDATE job_runs "
                        + "SET current_state = :state, "
                        + "SET started_at = CASE started_at WHEN NULL THEN CASE :state WHEN 1 THEN NOW() END ELSE started_at END, "
                        + "SET ended_at = CASE WHEN NULL THEN CASE :state WHEN 2 THEN NOW() END ELSE ended_at END "
                        + "where guid = :job_run_id";
  default void updateState(UUID jobRunID, Integer state) {
    try (final Handle handle = getHandle()) {
      handle.useTransaction(
          h -> {
            h.createUpdate("updateStateSQL")
                .bind("job_run_id", jobRunID)
                .bind("current_state", state)
                .execute();
            createJobRunStateDAO()
                .insert(UUID.randomUUID(), jobRunID, state);
          });
    } catch (Exception e) {
      // TODO: Add better error handling
      LOG.error(e.getMessage());
    }
  }

  @SqlQuery("SELECT * FROM job_runs WHERE guid = :guid")
  JobRun findJobRunById(@Bind("guid") UUID guid);
}
