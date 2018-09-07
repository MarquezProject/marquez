package marquez.db.dao;

import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.UUID;

public interface JobRunStateDAO extends SqlObject {
  static final Logger LOG = LoggerFactory.getLogger(JobRunStateDAO.class);

    @SqlUpdate(
            "INSERT INTO job_run_states (guid, started_at, transitioned_at, state) VALUES (:guid, :transitioned_at, :state)")
    void insert(
            @Bind("guid") final UUID guid,
            @Bind("transitioned_at") final Timestamp transitionedAt,
            @Bind("state") final Integer state);
}
