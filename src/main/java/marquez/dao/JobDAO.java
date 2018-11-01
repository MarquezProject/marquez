package marquez.dao;

import java.util.List;
import marquez.api.Job;
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
                    "INSERT INTO jobs (guid, name, current_owner_name)"
                        + " VALUES (:guid, :name, :current_owner_name)")
                .bind("guid", job.getGuid())
                .bind("name", job.getName())
                .bind("current_owner_name", job.getOwnerName())
                .execute();
          });
    } catch (Exception e) {
      // TODO: Add better error handling
      LOG.error(e.getMessage());
      throw e;
    }
  }

  @SqlQuery("SELECT * FROM jobs WHERE name = :name")
  Job findByName(@Bind("name") String name);

  @SqlQuery("SELECT * FROM jobs LIMIT :limit")
  List<Job> findAll(@Bind("limit") int limit);

  @SqlQuery("SELECT * FROM jobs LIMIT 1")
  List<Job> findAllInNamespace(@Bind("namespace") String namespace);
}
