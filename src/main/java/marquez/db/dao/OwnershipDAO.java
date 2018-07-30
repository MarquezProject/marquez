package marquez.db;

import java.util.List;
import marquez.api.Ownership;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface OwnershipDAO {
  @SqlQuery("SELECT * FROM ownerships WHERE id = :id")
  Ownership findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM ownerships")
  List<Ownership> findAll();
}
