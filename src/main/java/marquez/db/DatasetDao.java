package marquez.db;

import java.util.List;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.models.DatasetRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(DatasetRowMapper.class)
public interface DatasetDao {
  @SqlQuery("SELECT * FROM datasets LIMIT :limit OFFSET :offset")
  DatasetRow find(@Bind("namespace") String namespace, @Bind("limit") String urn);

  @SqlQuery("SELECT * FROM datasets LIMIT :limit OFFSET :offset")
  List<DatasetRow> findAll(
      @Bind("namespace") String namespace,
      @Bind("limit") Integer limit,
      @Bind("offset") Integer offset);
}
