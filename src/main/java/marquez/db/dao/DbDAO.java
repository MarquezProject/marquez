package marquez.db.dao;

import marquez.api.Db;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;
import java.util.UUID;

public interface DbDAO {
  @SqlQuery("SELECT * FROM dbs WHERE guid = :guid")
  Db findByGuid(@Bind("guid") UUID guid);

  @SqlQuery("SELECT * FROM dbs")
  List<Db> findAll();
}
