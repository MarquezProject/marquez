package marquez.db;

import java.util.UUID;
import marquez.db.mappers.JobRunStateRowMapper;
import marquez.service.models.JobRunState;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobRunStateRowMapper.class)
public interface JobRunStateDao {
  @SqlUpdate(
      "INSERT INTO job_run_states (guid, job_run_guid, state)"
          + "VALUES (:guid, :job_run_guid, :state)")
  void insert(
      @Bind("guid") final UUID guid,
      @Bind("job_run_guid") UUID job_run_guid,
      @Bind("state") final Integer state);

  @SqlQuery("SELECT * FROM job_run_states WHERE guid = :guid")
  JobRunState findById(@Bind("guid") UUID guid);

  @SqlQuery(
      "SELECT * FROM job_run_states WHERE job_run_guid = :jobRunGuid ORDER by transitioned_at DESC")
  JobRunState findByLatestJobRun(@Bind("jobRunGuid") UUID jobRunGuid);
}
