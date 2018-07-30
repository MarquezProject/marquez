package marquez.db.dao;

import java.util.List;
import marquez.api.Owner;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface OwnerDAO {
  @SqlQuery("SELECT * FROM owners WHERE id = :id")
  Owner findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM owners")
  List<Owner> findAll();
}
