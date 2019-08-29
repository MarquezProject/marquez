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
import marquez.common.models.NamespaceName;
import marquez.db.mappers.NamespaceRowMapper;
import marquez.db.models.NamespaceRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(NamespaceRowMapper.class)
public interface NamespaceDao {
  @SqlUpdate(
      "INSERT INTO namespaces (uuid, name, description, current_ownership) "
          + "VALUES (:uuid, :name, :description, :currentOwnerName) "
          + "ON CONFLICT (name) DO NOTHING")
  void insert(@BindBean NamespaceRow namespaceRow);

  @SqlQuery(
      "INSERT INTO namespaces (uuid, name, description, current_ownership) "
          + "VALUES (:uuid, :name, :description, :currentOwnerName) "
          + "ON CONFLICT (name) DO UPDATE "
          + "SET updated_at = NOW(), "
          + "    current_ownership = :currentOwnerName, "
          + "    description = :description "
          + "RETURNING *")
  Optional<NamespaceRow> insertAndGet(@BindBean NamespaceRow namespaceRow);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM namespaces WHERE name = :value)")
  boolean exists(@BindBean NamespaceName namespaceName);

  @SqlQuery("SELECT * FROM namespaces WHERE uuid = :uuid")
  Optional<NamespaceRow> findBy(UUID uuid);

  @SqlQuery("SELECT * FROM namespaces WHERE name = :value")
  Optional<NamespaceRow> findBy(@BindBean NamespaceName namespaceName);

  @SqlQuery("SELECT * FROM namespaces ORDER BY name LIMIT :limit OFFSET :offset")
  List<NamespaceRow> findAll(Integer limit, Integer offset);

  @SqlQuery("SELECT COUNT(*) FROM namespaces")
  Integer count();
}
