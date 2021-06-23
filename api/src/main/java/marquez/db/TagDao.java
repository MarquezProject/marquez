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
import java.util.Set;
import java.util.UUID;
import marquez.db.mappers.TagMapper;
import marquez.db.mappers.TagRowMapper;
import marquez.db.models.TagRow;
import marquez.service.models.Tag;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(TagRowMapper.class)
@RegisterRowMapper(TagMapper.class)
public interface TagDao {
  @SqlQuery(
      "INSERT INTO tags (uuid, created_at, updated_at, name) "
          + "VALUES (:uuid, :updatedAt, :updatedAt, :name) "
          + "ON CONFLICT(name) DO UPDATE SET updated_at = EXCLUDED.updated_at "
          + "RETURNING *")
  TagRow upsert(UUID uuid, Instant updatedAt, String name);

  @SqlQuery(
      "INSERT INTO tags (uuid, created_at, updated_at, name, description) "
          + "VALUES (:uuid, :updatedAt, :updatedAt, :name, :description) "
          + "ON CONFLICT(name) DO UPDATE SET "
          + "updated_at = EXCLUDED.updated_at, "
          + "description = EXCLUDED.description "
          + "RETURNING *")
  TagRow upsert(UUID uuid, Instant updatedAt, String name, String description);

  default Tag upsert(Tag tag) {
    Instant now = Instant.now();
    if (tag.getDescription().isPresent()) {
      upsert(UUID.randomUUID(), now, tag.getName().getValue(), tag.getDescription().get());
    } else {
      upsert(UUID.randomUUID(), now, tag.getName().getValue());
    }
    return find(tag.getName().getValue());
  }

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM tags WHERE name = :name)")
  boolean exists(String name);

  @SqlQuery("SELECT * FROM tags WHERE name = :name")
  Tag find(String name);

  @SqlQuery("SELECT * FROM tags ORDER BY name LIMIT :limit OFFSET :offset")
  Set<Tag> findAll(int limit, int offset);
}
