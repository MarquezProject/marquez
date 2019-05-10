/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetUrn;
import marquez.common.models.NamespaceName;
import marquez.db.mappers.DatasetRowExtendedMapper;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetRowExtended;
import marquez.db.models.DatasourceRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

public interface DatasetDao {
  @CreateSqlObject
  DatasourceDao createDatasourceDao();

  @CreateSqlObject
  DbTableVersionDao createDbTableVersionDao();

  @SqlUpdate(
      "INSERT INTO datasets (guid, namespace_guid, datasource_uuid, urn, description, name) "
          + "VALUES (:uuid, :namespaceUuid, :datasourceUuid, :urn, :description, :name)")
  @RegisterRowMapper(DatasetRowMapper.class)
  void insert(@BindBean DatasetRow datasetRow);

  @SqlQuery(
      "INSERT INTO datasets (guid, namespace_guid, datasource_uuid, urn, description, name) "
          + "VALUES (:uuid, :namespaceUuid, :datasourceUuid, :urn, :description, :name) "
          + "RETURNING *")
  @RegisterRowMapper(DatasetRowMapper.class)
  Optional<DatasetRow> insertAndGet(@BindBean DatasetRow datasetRow);

  @Deprecated
  @Transaction
  default void insertAll(
      DatasourceRow datasourceRow,
      DatasetRow datasetRow,
      DbTableInfoRow dbTableInfoRow,
      DbTableVersionRow dbTableVersionRow) {
    createDatasourceDao().insert(datasourceRow);
    insertAndGet(datasetRow);
    createDbTableVersionDao().insertAll(dbTableInfoRow, dbTableVersionRow);
    updateCurrentVersionUuid(datasetRow.getUuid(), dbTableVersionRow.getUuid());
  }

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM datasets WHERE urn = :value)")
  boolean exists(@BindBean DatasetUrn urn);

  @SqlUpdate(
      "UPDATE datasets "
          + "SET updated_at = NOW(), "
          + "    current_version_uuid = :currentVersionUuid "
          + "WHERE guid = :uuid")
  void updateCurrentVersionUuid(UUID uuid, UUID currentVersionUuid);

  @SqlQuery(
      "SELECT d.*, ds.urn AS datasource_urn "
          + "FROM datasets AS d "
          + "INNER JOIN datasources AS ds "
          + "    ON (ds.guid = d.datasource_uuid) "
          + "WHERE d.guid = :uuid")
  @RegisterRowMapper(DatasetRowExtendedMapper.class)
  Optional<DatasetRowExtended> findBy(UUID uuid);

  @SqlQuery(
      "SELECT d.*, ds.urn AS datasource_urn "
          + "FROM datasets AS d "
          + "INNER JOIN datasources AS ds "
          + "    ON (ds.guid = d.datasource_uuid) "
          + "WHERE d.urn = :value")
  @RegisterRowMapper(DatasetRowExtendedMapper.class)
  Optional<DatasetRowExtended> findBy(@BindBean DatasetUrn urn);

  @SqlQuery(
      "SELECT d.*, ds.urn AS datasource_urn "
          + "FROM datasets AS d "
          + "INNER JOIN namespaces AS ns "
          + "    ON (ns.guid = d.namespace_guid AND ns.name = :value) "
          + "INNER JOIN datasources AS ds "
          + "    ON (ds.guid = d.datasource_uuid)"
          + "ORDER BY ns.name "
          + "LIMIT :limit OFFSET :offset")
  @RegisterRowMapper(DatasetRowExtendedMapper.class)
  List<DatasetRowExtended> findAll(
      @BindBean NamespaceName namespaceName, Integer limit, Integer offset);

  @SqlQuery("SELECT COUNT(*) FROM datasets")
  Integer count();
}
