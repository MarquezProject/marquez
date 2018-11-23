package marquez.dao;

import java.util.List;
import java.util.UUID;
import marquez.core.models.Job;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterRowMapper(JobRow.class)
public interface JobDAO extends SqlObject {
  static final Logger LOG = LoggerFactory.getLogger(JobDAO.class);

  @CreateSqlObject
  OwnershipDAO createOwnershipDAO();

  default void insert(final Job job) {
    try (final Handle handle = getHandle()) {
      handle.useTransaction(
          h -> {
            h.createUpdate(
                    "INSERT INTO jobs (guid, name, current_owner_name, namespace_guid)"
                        + " VALUES (:guid, :name, :current_owner_name, :namespace_guid)")
                .bind("guid", job.getGuid())
                .bind("name", job.getName())
                .bind("current_owner_name", job.getOwnerName())
                .bind("namespace_guid", job.getNamespaceGuid())
                .execute();
          });
    } catch (Exception e) {
      // TODO: Add better error handling
      LOG.error(e.getMessage());
      throw e;
    }
  }

  @SqlQuery("SELECT * FROM jobs WHERE guid = :guid")
  Job findByID(@Bind("guid") UUID guid);

  @SqlQuery("SELECT * FROM jobs WHERE name = :name")
  Job findByName(@Bind("name") String name);

  String findAllByNamespaceNameSQL =
      "SELECT * "
          + " FROM jobs j"
          + " INNER JOIN namespaces n ON (j.namespace_guid = n.guid AND n.name = :ns_name)";

  @SqlQuery(findAllByNamespaceNameSQL)
  List<Job> findAllInNamespace(@Bind("ns_name") String namespaceName);
}
