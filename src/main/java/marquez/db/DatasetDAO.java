package marquez.db;

import java.util.List;
import marquez.api.Dataset;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface DatasetDAO {
  @SqlQuery("SELECT * FROM datasets WHERE id = :id")
  Dataset findById(@Bind("id") Long id);

  @SqlQuery("SELECT * FROM datasets")
  List<Dataset> findAll();
}
