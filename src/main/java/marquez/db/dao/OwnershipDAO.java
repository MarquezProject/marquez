package marquez.db.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface OwnershipDAO {
  static final Logger LOG = LoggerFactory.getLogger(OwnershipDAO.class);

  @SqlQuery(
      "INSERT INTO ownerships (job_id, owner_id) "
          + "VALUES ("
          + "(SELECT id FROM jobs WHERE name = :jobName),"
          + "(SELECT id FROM owners WHERE name = :ownerName)"
          + ")")
  int insert(@Bind("jobName") String jobName, @Bind("ownerName") String ownerName);
}
