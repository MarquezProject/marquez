package marquez.dao.deprecated;

import java.util.UUID;
import marquez.api.JobRun;
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
                    "INSERT INTO job_runs (guid, started_at, job_run_definition_guid, current_state) "
                        + "VALUES (:guid, :startedAt, :jobRunDefinitionGuid, :currentState)")
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

  default void updateState(UUID jobRunID, Integer state) {
    try (final Handle handle = getHandle()) {
      handle.useTransaction(
          h -> {
            h.createUpdate(
                    "UPDATE job_runs SET current_state = :state, started_at = :startedAt, ended_at = :endedAt where guid = :job_run_id")
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
