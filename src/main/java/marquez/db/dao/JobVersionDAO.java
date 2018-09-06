package marquez.db.dao;

import java.util.UUID;
import marquez.api.JobVersion;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobVersionRow.class)
public interface JobVersionDAO {
  @SqlQuery("SELECT * FROM job_versions WHERE version = :version")
  JobVersion findByVersion(@Bind("version") UUID version);

  @SqlUpdate("INSERT INTO job_versions(guid, version, job_guid, uri) VALUES (:guid, :version, :job_guid, :uri)")
  void insert(@Bind("guid") UUID guid, @Bind("version") UUID jobVersion, @Bind("job_guid") UUID jobGuid, @Bind("uri") String uri);
}
