package marquez.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.DataSourceRowMapper;
import marquez.db.models.DataSourceRow;
import marquez.db.sql.BindDataSourceRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DataSourceRowMapper.class)
public interface DataSourceDao {
  @SqlUpdate(
      "INSERT INTO data_sources (uuid, data_source, connection_url) "
          + "VALUES (:uuid, :data_source, :connection_url)")
  void insert(@BindDataSourceRow DataSourceRow dataSourceRow);

  @SqlQuery("SELECT * FROM data_sources WHERE uuid = :uuid")
  Optional<DataSourceRow> findBy(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM data_sources LIMIT :limit OFFSET :offset")
  List<DataSourceRow> findAll(@Bind("limit") Integer limit, @Bind("offset") Integer offset);
}
