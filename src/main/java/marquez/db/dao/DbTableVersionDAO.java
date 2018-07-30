package marquez.db.dao;

import java.util.List;
import marquez.api.DbTableVersion;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface DbTableVersionDAO {
  @SqlQuery("SELECT * FROM db_table_versions WHERE id = :id")
  DbTableVersion findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM db_table_versions")
  List<DbTableVersion> findAll();
}
