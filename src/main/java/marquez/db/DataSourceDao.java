package marquez.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.DataSourceRowMapper;
import marquez.db.models.DataSourceRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DataSourceRowMapper.class)
public interface DataSourceDao {
  @SqlUpdate(
      "INSERT INTO datasources (guid, name, connection_url) "
          + "VALUES (:uuid, :name, :connectionUrl)")
  void insert(@BindBean DataSourceRow dataSourceRow);

  @SqlQuery("SELECT * FROM datasources WHERE guid = :uuid")
  Optional<DataSourceRow> findBy(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM datasources LIMIT :limit OFFSET :offset")
  List<DataSourceRow> findAll(@Bind("limit") Integer limit, @Bind("offset") Integer offset);
}
