package marquez.db;

import java.util.List;
import marquez.api.IcebergTableVersion;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface IcebergTableVersionDAO {
  @SqlQuery("SELECT * FROM iceberg_table_versions WHERE id = :id")
  IcebergTableVersion findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM iceberg_table_versions")
  List<IcebergTableVersion> findAll();
}
