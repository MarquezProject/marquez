package marquez.db;

import java.util.UUID;
import marquez.common.models.Db;
import marquez.common.models.DbSchema;
import marquez.common.models.DbTable;
import marquez.db.mappers.DatasetRowMapper;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DatasetRowMapper.class)
public interface DbTableVersionDao {
  @CreateSqlObject
  DbTableInfoDao createDbTableInfoDao();

  @SqlUpdate(
      "INSERT INTO db_table_versions (uuid, dataset_uuid, db_table_info_uuid, db_table) "
          + "VALUES (:uuid, :datasetUuid, :dbTableInfoUuid, :dbTable.value)")
  void insert(
      @Bind("uuid") UUID uuid,
      @Bind("datasetUuid") UUID datasetUuid,
      @Bind("dbTableInfoUuid") UUID dbTableInfoUuid,
      @BindBean("dbTable") DbTable dbTable);

  @Transaction
  default void insert(UUID datasetUuid, Db db, DbSchema dbSchema, DbTable dbTable) {
    final UUID dbTableInfoUuid = UUID.randomUUID();
    createDbTableInfoDao().insert(dbTableInfoUuid, db, dbSchema);

    final UUID dbTableVersionUuid = UUID.randomUUID();
    insert(dbTableVersionUuid, datasetUuid, dbTableInfoUuid, dbTable);
  }
}
