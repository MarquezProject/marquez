package marquez.db;

import java.util.List;
import marquez.api.Db;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface DbDAO {
  @SqlQuery("SELECT * FROM dbs WHERE id = :id")
  Db findById(@Bind("id") Long id);

  @SqlQuery("SELECT * FROM dbs")
  List<Db> findAll();
}
