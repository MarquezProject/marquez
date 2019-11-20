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
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface DatasetDao {
  @SqlUpdate(
      "INSERT INTO datasets (uuid, type, created_at, updated_at, namespace_uuid, source_uuid, name, physical_name, description, current_version_uuid) "
          + "VALUES (:uuid, :type, :createdAt, :updatedAt, :namespaceUuid, :sourceUuid, :name, :physicalName, :description, :currentVersionUuid)")
  void insert(@BindBean DatasetRow row);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM datasets WHERE name = :name)")
  boolean exists(String name);

  @SqlUpdate(
      "UPDATE datasets "
          + "SET updated_at = :updatedAt, "
          + "    current_version_uuid = :currentVersionUuid "
          + "WHERE uuid = :rowUuid")
  void update(UUID rowUuid, Instant updatedAt, UUID currentVersionUuid);

  @SqlQuery(
      "SELECT d.*, s.name AS source_name "
          + "FROM datasets AS d "
          + "INNER JOIN sources AS s "
          + "  ON (s.uuid = d.source_uuid) "
          + "WHERE d.uuid = :rowUuid")
  @RegisterRowMapper(ExtendedDatasetRowMapper.class)
  Optional<ExtendedDatasetRow> findBy(UUID rowUuid);

  @SqlQuery(
      "SELECT d.*, s.name AS source_name "
          + "FROM datasets AS d "
          + "INNER JOIN sources AS s "
          + "  ON (s.uuid = d.source_uuid) "
          + "WHERE d.name = :name")
  @RegisterRowMapper(ExtendedDatasetRowMapper.class)
  Optional<ExtendedDatasetRow> findBy(String name);

  @SqlQuery("SELECT * FROM datasets WHERE uuid IN (<rowUuids>)")
  @RegisterRowMapper(DatasetRowMapper.class)
  List<DatasetRow> findAllInUuidList(@BindList(onEmpty = NULL_STRING) List<UUID> rowUuids);

  @SqlQuery("SELECT * FROM datasets WHERE name IN (<names>)")
  @RegisterRowMapper(DatasetRowMapper.class)
  List<DatasetRow> findAllInStringList(@BindList(onEmpty = NULL_STRING) List<String> names);

  @SqlQuery(
      "SELECT d.*, s.name AS source_name "
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
