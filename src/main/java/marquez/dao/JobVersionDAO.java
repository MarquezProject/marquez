package marquez.dao;

import java.util.List;
import java.util.UUID;
import marquez.core.models.JobVersion;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobVersionRow.class)
public interface JobVersionDAO {

  @SqlQuery("SELECT * FROM job_versions WHERE version = :version")
  JobVersion findByVersion(@Bind("version") UUID version);

  String findVersionsSQL =
      "SELECT jv.* \n"
          + "FROM job_versions jv\n"
          + "INNER JOIN jobs j ON (j.guid = jv.job_guid AND j.name=:job_name)"
          + "INNER JOIN namespaces n ON (n.guid = j.namespace_guid AND n.name=:namespace_name)\n"
          + "ORDER BY created_at";

  @SqlQuery(findVersionsSQL)
  List<JobVersion> find(@Bind("namespace_name") String namespace, @Bind("job_name") String jobName);

  String findLatestVersionSQL =
      "SELECT jv.* \n"
          + "FROM job_versions jv\n"
          + "INNER JOIN jobs j ON (j.guid = jv.job_guid AND j.name=:job_name)"
          + "INNER JOIN namespaces n ON (n.guid = j.namespace_guid AND n.name=:namespace_name)\n"
          + "ORDER BY created_at DESC \n"
          + "LIMIT 1";

  @SqlQuery(findLatestVersionSQL)
  JobVersion findLatest(@Bind("namespace_name") String namespace, @Bind("job_name") String jobName);

  @SqlUpdate(
      "INSERT INTO job_versions(guid, version, job_guid, uri) VALUES (:guid, :version, :job_guid, :uri)")
  void insert(
      @Bind("guid") UUID guid,
      @Bind("version") UUID jobVersion,
      @Bind("job_guid") UUID jobGuid,
      @Bind("uri") String uri);
}
