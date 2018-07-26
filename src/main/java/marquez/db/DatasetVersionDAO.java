package marquez.db;

import java.util.List;
import marquez.api.DatasetVersion;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface DatasetVersionDAO {
  @SqlQuery("SELECT * FROM dataset_versions WHERE id = :id")
  DatasetVersion findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM dataset_versions")
  List<DatasetVersion> findAll();
}
