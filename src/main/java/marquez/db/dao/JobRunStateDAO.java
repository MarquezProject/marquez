package marquez.db.dao;

import java.sql.Timestamp;
import java.util.UUID;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JobRunStateDAO extends SqlObject {
  static final Logger LOG = LoggerFactory.getLogger(JobRunStateDAO.class);

  @SqlUpdate(
      "INSERT INTO job_run_states (guid, job_run_guid, transitioned_at, state) VALUES (:guid, :job_run_guid, :transitioned_at, :state)")
  void insert(
      @Bind("guid") final UUID guid,
      @Bind("job_run_guid") UUID job_run_guid,
      @Bind("transitioned_at") final Timestamp transitionedAt,
      @Bind("state") final Integer state);
}
