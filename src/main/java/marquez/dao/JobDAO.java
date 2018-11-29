package marquez.dao;

import java.util.List;
import java.util.UUID;
import marquez.core.models.Job;
import marquez.core.models.JobVersion;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterRowMapper(JobRow.class)
public interface JobDAO extends SqlObject {
  static final Logger LOG = LoggerFactory.getLogger(JobDAO.class);

  @CreateSqlObject
  JobVersionDAO createJobVersionDAO();

  /* default void insertJob(final Job job) {
    try (final Handle handle = getHandle()) {
            h.createUpdate(
                    "INSERT INTO jobs (guid, name, namespace_guid)"
                        + " VALUES (:guid, :name, :namespace_guid)")
                .bind("guid", job.getGuid())
                .bind("name", job.getName())
                .bind("namespace_guid", job.getNamespaceGuid())
                .bind("current_version_guid", jobVersion.getGuid())
                .execute();
    } catch (Exception e) {
      // TODO: Add better error handling
      LOG.error(e.getMessage());
      throw e;
    }
  } */

  @SqlUpdate("INSERT INTO jobs (guid, name, namespace_guid) VALUES (:guid, :name, :namespaceGuid)")
  public void insert(@BindBean Job job);

  @SqlUpdate("UPDATE jobs SET current_version_guid = :version_guid WHERE guid = :job_guid")
  public void setCurrentVersionGuid(
      @Bind("job_guid") UUID jobGuid, @Bind("version_guid") UUID currentVersionGuid);

  @Transaction
  default void insertJobAndVersion(final Job job, final JobVersion jobVersion) {
    insert(job);
    createJobVersionDAO().insert(jobVersion);
    setCurrentVersionGuid(job.getGuid(), jobVersion.getGuid());
  }

  @SqlQuery("SELECT * FROM jobs WHERE guid = :guid")
  Job findByID(@Bind("guid") UUID guid);

  String findJobByNamespaceNameSQL =
      "SELECT j.*"
          + " FROM jobs j"
          + " INNER JOIN namespaces n ON (j.namespace_guid = n.guid AND n.name = :ns_name AND j.name = :job_name)";

  @SqlQuery(findJobByNamespaceNameSQL)
  Job findByName(@Bind("ns_name") String namespace, @Bind("job_name") String name);

  String findAllByNamespaceNameSQL =
      "SELECT j.*"
          + " FROM jobs j"
          + " INNER JOIN namespaces n ON (j.namespace_guid = n.guid AND n.name = :ns_name)";

  @SqlQuery(findAllByNamespaceNameSQL)
  List<Job> findAllInNamespace(@Bind("ns_name") String namespaceName);
}
