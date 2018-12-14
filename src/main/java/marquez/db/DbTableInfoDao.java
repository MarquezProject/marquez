package marquez.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.Dataset;
import marquez.common.models.Schema;
import marquez.db.mappers.DbTableInfoRowMapper;
import marquez.db.models.DbTableInfoRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DbTableInfoRowMapper.class)
public interface DbTableInfoDao {
  @SqlUpdate(
      "INSERT INTO db_table_info (uuid, db, table) "
          + "VALUES (:uuid, :dataset.value, :schema.value)")
  void insert(
      @Bind("uuid") UUID uuid,
      @BindBean("dataset") Dataset dataset,
      @BindBean("schema") Schema schema);

  @SqlQuery("SELECT * FROM dbs WHERE uuid = :uuid")
  Optional<DbTableInfoRow> find(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM dbs LIMIT :limit OFFSET :offset")
  List<DbTableInfoRow> findAll(@Bind("limit") Integer limit, @Bind("offset") Integer offset);
}
