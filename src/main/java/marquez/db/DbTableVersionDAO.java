package marquez.db;

import java.util.List;
import marquez.api.DbTableVersion;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface DbTableVersionDAO {
  @SqlQuery("SELECT * FROM db_table_versions WHERE id = :id")
  DbTableVersion findById(@Bind("id") Long id);

  @SqlQuery("SELECT * FROM db_table_versions")
  List<DbTableVersion> findAll();
}

