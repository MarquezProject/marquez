package marquez.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.DbTableInfoRowMapper;
import marquez.db.models.DbTableInfoRow;
import marquez.db.sql.BindDbTableInfoRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DbTableInfoRowMapper.class)
public interface DbTableInfoDao {
  @SqlUpdate("INSERT INTO db_table_infos (uuid, db, db_schema) VALUES (:uuid, :db, :db_schema)")
  void insert(@BindDbTableInfoRow DbTableInfoRow dbTableInfoRow);

  @SqlQuery("SELECT * FROM db_table_info WHERE uuid = :uuid")
  Optional<DbTableInfoRow> findBy(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM db_table_info LIMIT :limit OFFSET :offset")
  List<DbTableInfoRow> findAll(@Bind("limit") Integer limit, @Bind("offset") Integer offset);
}
