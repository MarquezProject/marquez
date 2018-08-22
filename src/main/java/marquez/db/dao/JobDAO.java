package marquez.db.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import marquez.api.Job;
import marquez.api.Ownership;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JobDAO extends SqlObject {
  static final Logger LOG = LoggerFactory.getLogger(JobDAO.class);

  @CreateSqlObject
  OwnershipDAO createOwnershipDAO();

  default void insert(final Job job) {
    try (final Handle handle = getHandle()) {
      handle.useTransaction(
          h -> {
            final int jobId =
                h.createUpdate(
                        "INSERT INTO jobs (name, nominal_time, category, description)"
                            + " VALUES (:name, :nominalTime, :category, :description)")
                    .bindBean(job)
                    .executeAndReturnGeneratedKeys()
                    .mapTo(int.class)
                    .findOnly();
            final int ownerId =
                h.createQuery("SELECT id FROM owners WHERE name = :name")
                    .bind("name", job.getOwnerName())
                    .mapTo(int.class)
                    .findOnly();
            final int ownershipId =
                createOwnershipDAO()
                    .insert(
                        new Ownership(
                            new Timestamp(System.currentTimeMillis()),
                            Optional.empty(),
                            jobId,
                            ownerId));
            updateOwnership(ownershipId, jobId);
            h.commit();
          });
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }
  }

  @SqlQuery("SELECT * FROM jobs")
  List<Job> findAll();

  @SqlQuery("SELECT * FROM jobs WHERE name = :jobName")
  Job findByName(final String jobName);

  @SqlUpdate("UPDATE jobs SET current_ownership = :ownershipId WHERE id = :jobId")
  void updateOwnership(@Bind("ownershipId") final int ownershipId, @Bind("jobId") final int jobId);
}
