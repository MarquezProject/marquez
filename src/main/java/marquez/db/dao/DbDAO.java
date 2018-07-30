package marquez.db;

import java.util.List;
import marquez.api.Db;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface DbDAO {
  @SqlQuery("SELECT * FROM dbs WHERE id = :id")
  Db findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM dbs")
  List<Db> findAll();
}
