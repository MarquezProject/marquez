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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.NamespaceName;
import marquez.db.exceptions.RowNotFoundException;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasourceRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.db.models.NamespaceRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DatasetRowMapper.class)
public interface DatasetDao {
  @CreateSqlObject
  NamespaceDao createNamespaceDao();

  @CreateSqlObject
  DatasourceDao createDatasourceDao();

  @CreateSqlObject
  DbTableVersionDao createDbTableVersionDao();

  @SqlQuery(
      "INSERT INTO datasets (uuid, namespace_uuid, datasource_uuid, name, urn, description) "
          + "VALUES (:uuid, :namespaceUuid, :datasourceUuid, :name, :urn, :description) "
          + "RETURNING *")
  Optional<DatasetRow> insertAndGet(@BindBean DatasetRow datasetRow);

  @Transaction
  default Optional<DatasetRow> insertAndGet(
      NamespaceName namespaceName, DatasourceUrn datasourceUrn, DatasetRow datasetRow)
      throws RowNotFoundException {
    final NamespaceRow namespaceRow =
        createNamespaceDao()
            .findBy(namespaceName)
            .orElseThrow(
                () ->
                    new RowNotFoundException(
                        "Namespace row not found: " + namespaceName.getValue()));
    final DatasourceRow datasourceRow =
        createDatasourceDao()
            .findBy(datasourceUrn)
            .orElseThrow(
                () ->
                    new RowNotFoundException(
                        "Datasource row not found: " + datasourceUrn.getValue()));
    datasetRow.setNamespaceUuid(namespaceRow.getUuid());
    datasetRow.setDatasourceUuid(datasourceRow.getUuid());
    return insertAndGet(datasetRow);
  }

  @Transaction
  default void insertAll(
      DatasourceRow datasourceRow,
      DatasetRow datasetRow,
      DbTableInfoRow dbTableInfoRow,
      DbTableVersionRow dbTableVersionRow) {
    createDatasourceDao().insert(datasourceRow);
    insertAndGet(datasetRow);
    createDbTableVersionDao().insertAll(dbTableInfoRow, dbTableVersionRow);
    updateCurrentVersion(datasetRow.getUuid(), Instant.now(), dbTableVersionRow.getUuid());
  }

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM datasets WHERE urn = :value)")
  boolean exists(@BindBean DatasetUrn datasetUrn);

  @SqlUpdate(
      "UPDATE datasets SET updated_at = :updatedAt, current_version_uuid = :currentVersionUuid "
          + "WHERE uuid = :uuid")
  void updateCurrentVersion(UUID uuid, Instant updatedAt, UUID currentVersion);

  @SqlQuery("SELECT * FROM datasets WHERE uuid = :uuid")
  Optional<DatasetRow> findBy(UUID uuid);

  @SqlQuery("SELECT * FROM datasets WHERE urn = :value")
  Optional<DatasetRow> findBy(@BindBean DatasetUrn datasetUrn);

  @SqlQuery(
      "SELECT * "
          + "FROM datasets d "
          + "INNER JOIN namespaces n "
          + "    ON (n.guid = d.namespace_uuid AND n.name = :value)"
          + "ORDER BY name "
          + "LIMIT :limit OFFSET :offset")
  List<DatasetRow> findAll(@BindBean NamespaceName namespaceName, Integer limit, Integer offset);
}
