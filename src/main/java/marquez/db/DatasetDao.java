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

import static org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling.NULL_STRING;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.mappers.ExtendedDatasetRowMapper;
import marquez.db.models.DatasetRow;
import marquez.db.models.ExtendedDatasetRow;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

public interface DatasetDao extends SqlObject {
  @Transaction
  default void insert(DatasetRow row) {
    getHandle()
        .createUpdate(
            "INSERT INTO datasets ("
                + "uuid, "
                + "type, "
                + "created_at, "
                + "updated_at, "
                + "namespace_uuid, "
                + "source_uuid, "
                + "name, "
                + "physical_name, "
                + "description"
                + ") VALUES ("
                + ":uuid, "
                + ":type, "
                + ":createdAt, "
                + ":updatedAt, "
                + ":namespaceUuid, "
                + ":sourceUuid, "
                + ":name, "
                + ":physicalName, "
                + ":description)")
        .bindBean(row)
        .execute();
    // Tags
    final Instant taggedAt = row.getCreatedAt();
    row.getTagUuids().forEach(tagUuid -> updateTags(row.getUuid(), tagUuid, taggedAt));
  }

  @SqlQuery(
      "SELECT EXISTS ("
          + "SELECT 1 FROM datasets AS d "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.uuid = d.namespace_uuid AND n.name = :namespaceName) "
          + "WHERE d.name = :datasetName)")
  boolean exists(String namespaceName, String datasetName);

  @SqlUpdate(
      "INSERT INTO datasets_tag_mapping (dataset_uuid, tag_uuid, tagged_at) "
          + "VALUES (:rowUuid, :tagUuid, :taggedAt) "
          + "ON CONFLICT DO NOTHING")
  void updateTags(UUID rowUuid, UUID tagUuid, Instant taggedAt);

  @SqlUpdate(
      "UPDATE datasets "
          + "SET updated_at = :lastModified, "
          + "    last_modified = :lastModified "
          + "WHERE uuid IN (<rowUuids>)")
  void updateLastModifed(
      @BindList(onEmpty = NULL_STRING) List<UUID> rowUuids, Instant lastModified);

  @SqlUpdate(
      "UPDATE datasets "
          + "SET updated_at = :updatedAt, "
          + "    current_version_uuid = :currentVersionUuid "
          + "WHERE uuid = :rowUuid")
  void updateVersion(UUID rowUuid, Instant updatedAt, UUID currentVersionUuid);

  @SqlQuery(
      "SELECT d.*, s.name AS source_name, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM datasets_tag_mapping "
          + "      WHERE dataset_uuid = d.uuid) AS tag_uuids "
          + "FROM datasets AS d "
          + "INNER JOIN sources AS s "
          + "  ON (s.uuid = d.source_uuid) "
          + "WHERE d.uuid = :rowUuid")
  @RegisterRowMapper(ExtendedDatasetRowMapper.class)
  Optional<ExtendedDatasetRow> findBy(UUID rowUuid);

  @SqlQuery(
      "SELECT d.*, s.name AS source_name, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM datasets_tag_mapping "
          + "      WHERE dataset_uuid = d.uuid) AS tag_uuids "
          + "FROM datasets AS d "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.uuid = d.namespace_uuid AND n.name = :namespaceName) "
          + "INNER JOIN sources AS s "
          + "  ON (s.uuid = d.source_uuid) "
          + "WHERE d.name = :datasetName")
  @RegisterRowMapper(ExtendedDatasetRowMapper.class)
  Optional<ExtendedDatasetRow> find(String namespaceName, String datasetName);

  @SqlQuery(
      "SELECT *, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM datasets_tag_mapping "
          + "      WHERE dataset_uuid = uuid) AS tag_uuids "
          + "FROM datasets WHERE uuid IN (<rowUuids>)")
  @RegisterRowMapper(DatasetRowMapper.class)
  List<DatasetRow> findAllInUuidList(@BindList(onEmpty = NULL_STRING) List<UUID> rowUuids);

  @SqlQuery(
      "SELECT d.*, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM datasets_tag_mapping "
          + "      WHERE dataset_uuid = d.uuid) AS tag_uuids "
          + "FROM datasets AS d "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.uuid = d.namespace_uuid AND n.name = :namespaceName) "
          + "WHERE d.name IN (<datasetNames>)")
  @RegisterRowMapper(DatasetRowMapper.class)
  List<DatasetRow> findAllInStringList(
      String namespaceName, @BindList(onEmpty = NULL_STRING) List<String> datasetNames);

  @SqlQuery(
      "SELECT d.*, s.name AS source_name, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM datasets_tag_mapping "
          + "      WHERE dataset_uuid = d.uuid) AS tag_uuids "
          + "FROM datasets AS d "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.uuid = d.namespace_uuid AND n.name = :namespaceName) "
          + "INNER JOIN sources AS s "
          + "  ON (s.uuid = d.source_uuid)"
          + "ORDER BY d.name "
          + "LIMIT :limit OFFSET :offset")
  @RegisterRowMapper(ExtendedDatasetRowMapper.class)
  List<ExtendedDatasetRow> findAll(String namespaceName, int limit, int offset);

  @SqlQuery("SELECT COUNT(*) FROM datasets")
  int count();
}
