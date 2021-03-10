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
import marquez.db.mappers.TagRowMapper;
import marquez.db.models.TagRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(TagRowMapper.class)
public interface TagDao {
  @SqlUpdate(
      "INSERT INTO tags (uuid, created_at, updated_at, name, description) "
          + "VALUES (:uuid, :createdAt, :updatedAt, :name, :description)")
  void insert(@BindBean TagRow row);

  @SqlQuery(
      "INSERT INTO tags (uuid, created_at, updated_at, name) "
          + "VALUES (:uuid, :updatedAt, :updatedAt, :name) "
          + "ON CONFLICT(name) DO UPDATE SET updated_at = EXCLUDED.updated_at "
          + "RETURNING *")
  TagRow upsert(UUID uuid, Instant updatedAt, String name);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM tags WHERE name = :name)")
  boolean exists(String name);

  @SqlQuery("SELECT * FROM tags WHERE uuid = :rowUuid")
  Optional<TagRow> findBy(UUID rowUuid);

  @SqlQuery("SELECT * FROM tags WHERE name = :name")
  Optional<TagRow> findBy(String name);

  @SqlQuery("SELECT * FROM tags WHERE uuid IN (<rowUuids>)")
  List<TagRow> findAllIn(@BindList(onEmpty = NULL_STRING) UUID... rowUuids);

  @SqlQuery("SELECT * FROM tags WHERE name IN (<names>)")
  List<TagRow> findAllIn(@BindList(onEmpty = NULL_STRING) String... names);

  @SqlQuery("SELECT * FROM tags ORDER BY name LIMIT :limit OFFSET :offset")
  List<TagRow> findAll(int limit, int offset);

  @SqlQuery("SELECT COUNT(*) FROM tags")
  int count();
}
