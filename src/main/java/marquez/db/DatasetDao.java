package marquez.db;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSource;
import marquez.common.models.Dataset;
import marquez.common.models.Db;
import marquez.common.models.Description;
import marquez.common.models.Namespace;
import marquez.common.models.Schema;
import marquez.common.models.Table;
import marquez.common.models.Urn;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.models.DatasetRow;
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
      "INSERT INTO datasets (uuid, urn, namespace_uuid, datasource_uuid, description) "
          + "VALUES (:uuid, :urn.value, :namespaceUuid, :dataSourceUuid, :description.value)")
  void insert(
      @Bind("uuid") UUID uuid,
      @BindBean("urn") Urn urn,
      @Bind("namespaceUuid") UUID namespaceUuid,
      @Bind("dataSourceUuid") UUID dataSourceUuid,
      @BindBean("description") Description description);

  @Transaction
  default UUID insert(
      Namespace namespace,
      DataSource dataSource,
      ConnectionUrl connectionUrl,
      Db db,
      Schema schema,
      Table table,
      Description description) {
    final UUID dataSourceUuid = UUID.randomUUID();
    createDataSourceDao().insert(dataSourceUuid, dataSource, connectionUrl);

    final UUID datasetUuid = UUID.randomUUID();
    final String qualifiedName = schema.getValue() + '.' + table.getValue();
    final Dataset dataset = Dataset.of(qualifiedName);
    final Urn urn = Urn.of(namespace, dataset);
    insert(datasetUuid, urn, null, dataSourceUuid, description);
    createDbTableVersionDao().insert(datasetUuid, db, schema, table);
    return datasetUuid;
  }

  @SqlUpdate(
      "UPDATE datasets SET updated_at = :updatedAt, current_version_uuid = :versionUuid"
          + "WHERE uuid = :uuid")
  void updateCurrentVersion(
      @Bind("uuid") UUID uuid,
      @Bind("updatedAt") Instant updatedAt,
      @Bind("versionUuid") UUID versionUuid);

  @SqlQuery("SELECT * FROM datasets WHERE uuid = :uuid")
  Optional<DatasetRow> findBy(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM datasets WHERE urn = :urn.value")
  Optional<DatasetRow> findBy(@BindBean("urn") Urn urn);

  @SqlQuery("SELECT * FROM datasets LIMIT :limit OFFSET :offset")
  List<DatasetRow> findAll(
      @BindBean("namespace") Namespace namespace,
      @Bind("limit") Integer limit,
      @Bind("offset") Integer offset);
}
