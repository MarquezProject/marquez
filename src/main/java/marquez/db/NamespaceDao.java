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
import marquez.common.models.NamespaceName;
import marquez.db.mappers.NamespaceRowMapper;
import marquez.service.models.Namespace;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(NamespaceRowMapper.class)
public interface NamespaceDao {
  @SqlUpdate(
      "INSERT INTO namespaces(guid, name, description, current_ownership) "
          + "VALUES(:guid, :name, :description, :ownerName) "
          + "ON CONFLICT DO NOTHING")
  void insert(@BindBean Namespace namespace);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM namespaces WHERE name = :value)")
  boolean exists(@BindBean NamespaceName namespaceName);

  @SqlQuery("SELECT * FROM namespaces WHERE name = :value")
  Optional<Namespace> findBy(@BindBean NamespaceName namespaceName);

  @SqlQuery("SELECT * FROM namespaces")
  List<Namespace> findAll();
}
