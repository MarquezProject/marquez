package marquez.db;

import java.util.List;
import marquez.api.Ownership;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface OwnershipDAO {
  @SqlQuery("SELECT * FROM ownerships WHERE id = :id")
  Ownership findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM ownerships")
  List<Ownership> findAll();
}
