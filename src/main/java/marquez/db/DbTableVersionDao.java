package marquez.db;

import java.util.UUID;
import marquez.db.mappers.DatasetRowMapper;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DatasetRowMapper.class)
public interface DbTableVersionDao {
  @CreateSqlObject
  DbTableInfoDao createDbTableInfoDao();

  @Transaction
  default void insert(String db, String schema, String table, String description) {
    final UUID dataSourceUuid = UUID.randomUUID();
    // createDbTableInfoDao().insert()
    // insert(UUID.randomUUID(), null, null, dataSourceUuid, description);
  }
}
