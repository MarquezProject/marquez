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
import marquez.common.models.TagName;
import marquez.db.mappers.TagRowMapper;
import marquez.db.models.TagRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(TagRowMapper.class)
public interface TagDao {
  @SqlUpdate("INSERT INTO tags (uuid, name, description, created_at, updated_at) VALUES (:uuid, :name, :description, :createdAt, :updatedAt)")
  void insert(@BindBean TagRow row);

  @SqlQuery(
      "INSERT INTO tags (uuid, name, description, created_at, updated_at) "
          + "VALUES (:uuid, :name, :description, :createdAt, :updatedAt) "
          + "RETURNING *")
  Optional<TagRow> insertAndGet(@BindBean TagRow row);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM tags WHERE name = :value)")
  boolean exists(@BindBean TagName name);

  @SqlQuery("SELECT * FROM tags WHERE uuid = :uuid")
  Optional<TagRow> findBy(UUID uuid);

  @SqlQuery("SELECT * FROM tags WHERE uuid in (<uuids>)")
  List<TagRow> findBy(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<UUID> uuids);

  @SqlQuery("SELECT * FROM tags WHERE name = :value")
  Optional<TagRow> findBy(@BindBean TagName name);

  @SqlQuery("SELECT * FROM tags ORDER BY name LIMIT :limit OFFSET :offset")
  List<TagRow> findAll(Integer limit, Integer offset);

  @SqlQuery("SELECT COUNT(*) FROM tags")
  Integer count();
}
