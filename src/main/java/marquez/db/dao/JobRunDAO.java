package marquez.db.dao;

import static marquez.api.JobRunState.State.toInt;

import java.util.List;
import java.util.UUID;

import marquez.api.Job;
import marquez.api.JobRun;
import marquez.api.JobRunState;
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
                .insert(
                    UUID.randomUUID(),
                    jobRun.getGuid(),
                    jobRun.getCreatedAt(),
                    toInt(JobRunState.State.NEW));
          });
    } catch (Exception e) {
      // TODO: Add better error handling
      LOG.error(e.getMessage());
    }
  }

    @SqlQuery("SELECT * FROM job_runs WHERE guid = :guid")
    JobRun findJobRunById(@Bind("guid") UUID guid);
}
