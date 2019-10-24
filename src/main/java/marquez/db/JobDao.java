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
import marquez.db.mappers.JobRowMapper;
import marquez.db.models.JobRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobRowMapper.class)
public interface JobDao {
  @SqlUpdate(
      "INSERT INTO jobs (uuid, type, created_at, updated_at, namespace_uuid, name, description, current_version_uuid) "
          + "VALUES (:uuid, :type, :createdAt, :updatedAt, :namespaceUuid, :name, :description, :currentVersionUuid)")
  void insert(@BindBean JobRow row);

  @SqlQuery(
      "SELECT EXISTS (SELECT 1 FROM jobs AS j "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.name = :namespaceName AND "
          + "      j.namespace_uuid = n.uuid AND "
          + "      j.name = :jobName))")
  boolean exists(String namespaceName, String jobName);

  @SqlUpdate(
      "UPDATE jobs "
          + "SET updated_at = :updatedAt, "
          + "    current_version_uuid = :currentVersionUuid "
          + "WHERE uuid = :rowUuid")
  void update(UUID rowUuid, Instant updatedAt, UUID currentVersionUuid);

  @SqlQuery(
      "SELECT j.* FROM jobs AS j "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.name = :namespaceName AND "
          + "      j.namespace_uuid = n.uuid AND "
          + "      j.name = :jobName)")
  Optional<JobRow> findBy(String namespaceName, String jobName);

  @SqlQuery(
      "SELECT j.* FROM jobs AS j "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.name = :namespaceName AND j.namespace_uuid = n.uuid) "
          + "ORDER BY j.name "
          + "LIMIT :limit OFFSET :offset")
  List<JobRow> findAll(String namespaceName, int limit, int offset);

  @SqlQuery("SELECT COUNT(*) FROM jobs")
  int count();
}
