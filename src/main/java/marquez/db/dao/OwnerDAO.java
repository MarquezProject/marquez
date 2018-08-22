package marquez.db.dao;

import marquez.api.Owner;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface OwnerDAO {
  @SqlUpdate("INSERT INTO owners (name) VALUES (:name)")
  void insert(@BindBean final Owner owner);
}
