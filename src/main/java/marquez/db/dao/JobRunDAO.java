package marquez.db.dao;

import marquez.api.JobRun;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.sqlobject.SqlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JobRunDAO extends SqlObject {
    static final Logger LOG = LoggerFactory.getLogger(JobRunDAO.class);

    default void insert(final JobRun jobRun) {
        try (final Handle handle = getHandle()) {
            handle.useTransaction(
                    h -> {
                        h.createUpdate(
                                "INSERT INTO job_runs (started_at, job_run_definition_guid, current_state)"
                                        + " VALUES (:startedAt, :jobRunDefinitionId, :currentState)")
                                .bindBean(jobRun)
                                .execute();
                        h.commit();
                    });
        } catch (Exception e) {
            // TODO: Add better error handling
            LOG.error(e.getMessage());
        }
    }
}
