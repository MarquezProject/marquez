package marquez.db;

import java.util.List;
import marquez.api.Owner;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface OwnerDAO {
  @SqlQuery("SELECT * FROM owners WHERE id = :id")
  Owner findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM owners")
  List<Owner> findAll();
}
