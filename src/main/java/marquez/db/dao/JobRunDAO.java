package marquez.db.dao;

import java.sql.Timestamp;
import java.util.UUID;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JobRunDAO extends SqlObject {
  static final Logger LOG = LoggerFactory.getLogger(JobRunDAO.class);

  @SqlUpdate(
      "INSERT INTO job_runs (guid, started_at, job_run_definition_guid, current_state) VALUES (:guid, :started_at, :job_run_definition_guid, :current_state)")
  void insert(
      @Bind("guid") final UUID guid,
      @Bind("started_at") final Timestamp started_at,
      @Bind("job_run_definition_guid") final UUID job_run_definition_guid,
      @Bind("current_state") final Integer current_state);
}
