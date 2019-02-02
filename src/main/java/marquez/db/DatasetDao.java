package marquez.db;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetUrn;
import marquez.common.models.NamespaceName;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DatasetRowMapper.class)
public interface DatasetDao {
  @CreateSqlObject
  DataSourceDao createDataSourceDao();

  @CreateSqlObject
  DbTableVersionDao createDbTableVersionDao();

  @SqlUpdate(
      "INSERT INTO datasets (uuid, namespace_uuid, datasource_uuid, urn, description) "
          + "VALUES (:uuid, :namespaceUuid, :datasourceUuid, :urn, :description)")
  void insert(@BindBean DatasetRow datasetRow);

  @Transaction
  default void insertAll(
      DataSourceRow dataSourceRow,
      DatasetRow datasetRow,
      DbTableInfoRow dbTableInfoRow,
      DbTableVersionRow dbTableVersionRow) {
    createDataSourceDao().insert(dataSourceRow);
    insert(datasetRow);
    createDbTableVersionDao().insertAll(dbTableInfoRow, dbTableVersionRow);
  }

  @SqlUpdate(
      "UPDATE datasets SET updated_at = :updatedAt, current_version = :currentVersion"
          + "WHERE uuid = :uuid")
  void updateCurrentVersion(
      @Bind("uuid") UUID uuid,
      @Bind("updatedAt") Instant updatedAt,
      @Bind("currentVersion") UUID currentVersion);

  @SqlQuery("SELECT * FROM datasets WHERE uuid = :uuid")
  Optional<DatasetRow> findBy(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM datasets WHERE urn = :urn.value")
  Optional<DatasetRow> findBy(@BindBean("urn") DatasetUrn urn);

  @SqlQuery("SELECT * FROM datasets LIMIT :limit OFFSET :offset")
  List<DatasetRow> findAll(
      @Bind("namespace") NamespaceName namespaceName,
      @Bind("limit") Integer limit,
      @Bind("offset") Integer offset);
}
