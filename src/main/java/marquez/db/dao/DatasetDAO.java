package marquez.db.dao;

import java.util.List;
import java.util.UUID;
import marquez.api.Dataset;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface DatasetDAO {
  @SqlQuery("SELECT * FROM datasets WHERE guid = :guid")
  Dataset findByGuid(@Bind("guid") UUID guid);

  @SqlQuery("SELECT * FROM datasets")
  List<Dataset> findAll();
}
