package marquez.db.dao;

import java.util.UUID;
import marquez.api.JobRunDefinition;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobRunDefinitionRow.class)
public interface JobRunDefinitionDAO {
  @SqlQuery("SELECT * FROM job_run_definitions WHERE guid = :guid")
  JobRunDefinition findByName(@Bind("guid") UUID guid);

  @SqlQuery("SELECT * FROM job_run_definitions WHERE content_hash = :hash")
  JobRunDefinition findByHash(@Bind("hash") UUID hash);

  @SqlUpdate(
      "INSERT INTO job_run_definitions (guid, content_hash, job_version_guid, run_args_json) VALUES (:guid, :hash, :job_version_guid, :run_args)")
  void insert(
      @Bind("guid") final UUID guid,
      @Bind("hash") final UUID definitionHash,
      @Bind("job_version_guid") final UUID job_version_guid,
      @Bind("run_args") final String runArgsJson);
}
