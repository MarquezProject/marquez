package marquez.db.dao;

import java.util.UUID;
import marquez.api.JobRunDefinition;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobRunDefinitionRow.class)
public interface JobRunDefinitionDAO {
  @SqlQuery(
      "SELECT jrd.guid, jrd.job_version_guid, jrd.run_args_json, jrd.nominal_time, j.name, j.current_owner_name, jv.uri FROM job_run_definitions jrd INNER JOIN job_versions jv ON (jv.guid = jrd.job_version_guid) INNER JOIN jobs j ON(j.guid = jv.job_guid) WHERE jrd.guid = :guid")
  JobRunDefinition findByGuid(@Bind("guid") UUID guid);

  @SqlQuery(
      "SELECT jrd.guid, jrd.job_version_guid, jrd.run_args_json, jrd.nominal_time, j.name, j.current_owner_name, jv.uri FROM job_run_definitions jrd INNER JOIN job_versions jv ON (jv.guid = jrd.job_version_guid) INNER JOIN jobs j ON(j.guid = jv.job_guid) WHERE jrd.content_hash = :hash")
  JobRunDefinition findByHash(@Bind("hash") UUID hash);

  @SqlUpdate(
      "INSERT INTO job_run_definitions (guid, content_hash, job_version_guid, run_args_json) VALUES (:guid, :hash, :job_version_guid, :run_args)")
  void insert(
      @Bind("guid") final UUID guid,
      @Bind("hash") final UUID definitionHash,
      @Bind("job_version_guid") final UUID job_version_guid,
      @Bind("run_args") final String runArgsJson);
}
