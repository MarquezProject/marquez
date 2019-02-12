package marquez.db;

import marquez.db.mappers.DbTableVersionRowMapper;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DbTableVersionRowMapper.class)
public interface DbTableVersionDao {
  @CreateSqlObject
  DbTableInfoDao createDbTableInfoDao();

  @SqlUpdate(
      "INSERT INTO db_table_versions (guid, dataset_guid, db_table_info_uuid, db_table_name) "
          + "VALUES (:uuid, :datasetUuid, :dbTableInfoUuid, :dbTable)")
  void insert(@BindBean DbTableVersionRow dbTableVersionRow);

  @Transaction
  default void insertAll(DbTableInfoRow dbTableInfoRow, DbTableVersionRow dbTableVersionRow) {
    createDbTableInfoDao().insert(dbTableInfoRow);
    insert(dbTableVersionRow);
  }
}
