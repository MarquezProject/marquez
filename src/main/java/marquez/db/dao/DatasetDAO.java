package marquez.db;

import java.util.List;
import marquez.api.Dataset;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface DatasetDAO {
  @SqlQuery("SELECT * FROM datasets WHERE id = :id")
  Dataset findById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM datasets")
  List<Dataset> findAll();
}
