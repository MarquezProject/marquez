package marquez.db.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface OwnerDAO {
  @SqlUpdate("INSERT INTO owners (name) VALUES (:name)")
  void insert(@Bind("name") String name);
}
