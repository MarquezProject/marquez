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
import marquez.db.mappers.DatasetFieldRowMapper;
import marquez.db.models.DatasetFieldRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DatasetFieldRowMapper.class)
public interface DatasetFieldDao {
  @Transaction
  default void insertAll(List<DatasetFieldRow> rows) {
    rows.forEach(row -> insert(row));
  }

  @SqlUpdate(
      "INSERT INTO dataset_fields ("
          + "uuid, "
          + "type, "
          + "created_at, "
          + "updated_at, "
          + "dataset_uuid, "
          + "name, "
          + "description"
          + ") VALUES ("
          + ":uuid, "
          + ":type, "
          + ":createdAt, "
          + ":updatedAt, "
          + ":datasetUuid, "
          + ":name, "
          + ":description)")
  void insert(@BindBean DatasetFieldRow row);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM dataset_fields WHERE name = :name)")
  boolean exists(String name);

  @SqlUpdate(
      "INSERT INTO dataset_fields_tag_mapping (dataset_field_uuid, tag_uuid, tagged_at) "
          + "VALUES (:rowUuid, :tagUuid, :taggedAt)")
  void updateTags(UUID rowUuid, UUID tagUuid, Instant taggedAt);

  @SqlQuery(
      "SELECT *, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM dataset_fields_tag_mapping "
          + "      WHERE dataset_field_uuid = uuid) AS tag_uuids "
          + " FROM dataset_fields WHERE uuid = :rowUuid")
  Optional<DatasetFieldRow> findBy(UUID rowUuid);

  @SqlQuery(
      "SELECT *, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM dataset_fields_tag_mapping "
          + "      WHERE dataset_field_uuid = uuid) AS tag_uuids "
          + " FROM dataset_fields WHERE dataset_uuid = :datasetUuid AND name = :name")
  Optional<DatasetFieldRow> find(UUID datasetUuid, String name);

  @SqlQuery(
      "SELECT *, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM dataset_fields_tag_mapping "
          + "      WHERE dataset_field_uuid = uuid) AS tag_uuids "
          + " FROM dataset_fields WHERE uuid IN (<rowUuids>)")
  List<DatasetFieldRow> findAllInUuidList(@BindList(onEmpty = NULL_STRING) List<UUID> rowUuids);

  @SqlQuery(
      "SELECT *, "
          + "ARRAY(SELECT tag_uuid "
          + "      FROM dataset_fields_tag_mapping "
          + "      WHERE dataset_field_uuid = uuid) AS tag_uuids "
          + " FROM dataset_fields WHERE dataset_uuid = :datasetUuid ORDER BY created_at")
  List<DatasetFieldRow> findAll(UUID datasetUuid);

  @SqlQuery("SELECT COUNT(*) FROM dataset_fields")
  int count();
}
