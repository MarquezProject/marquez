package marquez.db;

import marquez.db.mappers.DbTableVersionRowMapper;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.db.sql.BindDbTableVersionRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DbTableVersionRowMapper.class)
public interface DbTableVersionDao {
  @CreateSqlObject
  DbTableInfoDao createDbTableInfoDao();

  @SqlUpdate(
      "INSERT INTO db_table_versions (uuid, dataset_uuid, db_table_info_uuid, db_table) "
          + "VALUES (:uuid, :dataset_uuid, :db_table_info_uuid, :db_table)")
  void insert(@BindDbTableVersionRow DbTableVersionRow dbTableVersionRow);

  @Transaction
  default void insertAll(DbTableInfoRow dbTableInfoRow, DbTableVersionRow dbTableVersionRow) {
    createDbTableInfoDao().insert(dbTableInfoRow);
    insert(dbTableVersionRow);
  }
}
