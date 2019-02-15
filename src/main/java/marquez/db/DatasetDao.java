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
      "INSERT INTO datasets (guid, namespace_guid, datasource_uuid, urn, description) "
          + "VALUES (:uuid, :namespaceUuid, :dataSourceUuid, :urn, :description)")
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
    updateCurrentVersion(datasetRow.getUuid(), Instant.now(), dbTableVersionRow.getUuid());
  }

  @SqlUpdate(
      "UPDATE datasets SET updated_at = :updatedAt, current_version_uuid = :currentVersion "
          + "WHERE guid = :uuid")
  void updateCurrentVersion(
      @Bind("uuid") UUID uuid,
      @Bind("updatedAt") Instant updatedAt,
      @Bind("currentVersion") UUID currentVersion);

  @SqlQuery("SELECT * FROM datasets WHERE uuid = :uuid")
  Optional<DatasetRow> findBy(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM datasets WHERE urn = :urn.value")
  Optional<DatasetRow> findBy(@BindBean("urn") DatasetUrn urn);

  @SqlQuery(
      "SELECT * "
          + "FROM datasets d "
          + "INNER JOIN namespaces n "
          + "     ON (n.guid = d.namespace_guid AND n.name=:namespace.value)"
          + "LIMIT :limit OFFSET :offset")
  List<DatasetRow> findAll(
      @BindBean("namespace") NamespaceName namespaceName,
      @Bind("limit") Integer limit,
      @Bind("offset") Integer offset);
}
