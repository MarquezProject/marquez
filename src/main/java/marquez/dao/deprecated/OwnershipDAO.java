package marquez.dao.deprecated;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public interface OwnershipDAO {
  static final Logger LOG = LoggerFactory.getLogger(OwnershipDAO.class);

  @SqlQuery(
      "INSERT INTO ownerships (job_guid, owner_guid) "
          + "VALUES ("
          + "(SELECT guid FROM jobs WHERE name = :jobName),"
          + "(SELECT guid FROM owners WHERE name = :ownerName)"
          + ")")
  int insert(@Bind("jobName") String jobName, @Bind("ownerName") String ownerName);
}
