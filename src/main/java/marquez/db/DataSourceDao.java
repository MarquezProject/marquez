package marquez.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.ConnectionUrl;
import marquez.db.mappers.DataSourceRowMapper;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DataSourceType;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DataSourceRowMapper.class)
public interface DataSourceDao {
  @SqlUpdate(
      "INSERT INTO data_sources (uuid, type, connection_url) "
          + "VALUES (:uuid, :type, :connectionUrl.value)")
  void insert(
      @Bind("uuid") UUID uuid,
      @Bind("type") DataSourceType type,
      @BindBean("connectionUrl") ConnectionUrl connectionUrl);

  @SqlQuery("SELECT * FROM data_sources WHERE uuid = :uuid")
  Optional<DataSourceRow> find(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM data_sources LIMIT :limit OFFSET :offset")
  List<DataSourceRow> findAll(@Bind("limit") Integer limit, @Bind("offset") Integer offset);
}
