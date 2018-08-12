package marquez.db.dao;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.sqlobject.SqlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface OwnershipDAO extends SqlObject {
  static final Logger LOG = LoggerFactory.getLogger(OwnershipDAO.class);

  default void insert(final String jobName, final String ownerName) {
    try (final Handle handle = getHandle()) {
      handle.createUpdate(
                    "INSERT INTO ownerships"
                        + "VALUES ("
                        + "job_id, (SELECT id FROM jobs WHERE name = :jobName),"
                        + "owner_id, (SELECT id FROM owners WHERE name = :ownerName)"
                        + ")")
                .bind("jobName", jobName)
                .bind("ownerName", ownerName)
                .execute();
    } catch (Exception e) {
      // TODO: Add better error handling
      LOG.error(e.getMessage());
    }
  }
}
