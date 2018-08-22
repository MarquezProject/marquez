package marquez.db.dao;

import marquez.api.Ownership;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface OwnershipDAO {
  @SqlUpdate(
      "INSERT INTO ownerships (started_at, endeded_at, job_id, owner_id) "
          + "VALUES (:startedAt, :endedAt, :jobId, :ownerId)")
  @GetGeneratedKeys
  int insert(@BindBean final Ownership ownership);
}
