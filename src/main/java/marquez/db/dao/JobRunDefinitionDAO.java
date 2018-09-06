package marquez.db.dao;

import java.util.UUID;
import marquez.api.Owner;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface JobRunDefinitionDAO {
  @SqlQuery("SELECT * FROM job_run_definitions WHERE guid = :guid")
  Owner findByName(@Bind("guid") UUID guid);

  @SqlUpdate(
      "INSERT INTO job_run_definitions (guid, job_version_guid, run_args_json) VALUES (:guid, :job_version_guid, :run_args)")
  void insert(
      @Bind("guid") final UUID guid,
      @Bind("job_version_guid") final UUID job_version_guid,
      @Bind("run_args") final String runArgsJson);
}
