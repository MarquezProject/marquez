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

import static marquez.db.OpenLineageDao.DEFAULT_NAMESPACE_OWNER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.db.mappers.JobRowMapper;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.service.models.JobMeta;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;

@RegisterRowMapper(JobRowMapper.class)
public interface JobDao extends MarquezDao {
  @SqlQuery(
      "SELECT EXISTS (SELECT 1 FROM jobs AS j "
          + "WHERE j.namespace_name= :namespaceName AND "
          + " j.name = :jobName)")
  boolean exists(String namespaceName, String jobName);

  /**
   * Updates the current version of the job
   *
   * @param rowUuid the jobs.uuid
   * @param updatedAt when it was updated
   * @param currentVersionUuid job_versions.uuid for the current version
   */
  @SqlUpdate(
      "UPDATE jobs "
          + "SET updated_at = :updatedAt, "
          + "    current_version_uuid = :currentVersionUuid "
          + "WHERE uuid = :rowUuid")
  void updateVersion(UUID rowUuid, Instant updatedAt, UUID currentVersionUuid);

  @SqlQuery(
      "SELECT j.*, n.name AS namespace_name FROM jobs AS j "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.name = :namespaceName AND "
          + "      j.namespace_uuid = n.uuid AND "
          + "      j.name = :jobName)")
  Optional<JobRow> find(String namespaceName, String jobName);

  @SqlQuery(
      "SELECT j.* FROM jobs AS j "
          + "WHERE namespace_name = :namespaceName "
          + "ORDER BY j.name "
          + "LIMIT :limit OFFSET :offset")
  List<JobRow> findAll(String namespaceName, int limit, int offset);

  @SqlQuery("SELECT COUNT(*) FROM jobs")
  int count();

  default JobRow upsert(
      NamespaceName namespaceName, JobName jobName, JobMeta jobMeta, ObjectMapper mapper) {
    Instant createdAt = Instant.now();
    NamespaceRow namespace =
        createNamespaceDao()
            .upsert(
                UUID.randomUUID(), createdAt, namespaceName.getValue(), DEFAULT_NAMESPACE_OWNER);
    JobContextRow contextRow =
        createJobContextDao()
            .upsert(
                UUID.randomUUID(),
                createdAt,
                Utils.toJson(jobMeta.getContext()),
                Utils.checksumFor(jobMeta.getContext()));

    return upsert(
        UUID.randomUUID(),
        jobMeta.getType(),
        createdAt,
        namespace.getUuid(),
        namespace.getName(),
        jobName.getValue(),
        jobMeta.getDescription().orElse(null),
        contextRow.getUuid(),
        toUrlString(jobMeta.getLocation().orElse(null)),
        toJson(jobMeta.getInputs(), mapper),
        toJson(jobMeta.getOutputs(), mapper));
  }

  default String toUrlString(URL url) {
    if (url == null) {
      return null;
    }
    return url.toString();
  }

  default PGobject toJson(Set<DatasetId> dataset, ObjectMapper mapper) {
    try {
      PGobject jsonObject = new PGobject();
      jsonObject.setType("json");
      jsonObject.setValue(mapper.writeValueAsString(dataset));
      return jsonObject;
    } catch (Exception e) {
      return null;
    }
  }

  @SqlQuery(
      "INSERT INTO jobs ("
          + "uuid, "
          + "type, "
          + "created_at, "
          + "updated_at, "
          + "namespace_uuid, "
          + "namespace_name, "
          + "name, "
          + "description,"
          + "job_context_template_uuid,"
          + "location_template,"
          + "inputs_template,"
          + "outputs_template "
          + ") VALUES ( "
          + ":uuid, "
          + ":type, "
          + ":now, "
          + ":now, "
          + ":namespaceUuid, "
          + ":namespaceName, "
          + ":name, "
          + ":description, "
          + ":jobContextUuid, "
          + ":location, "
          + ":inputs, "
          + ":outputs "
          + ") ON CONFLICT (name, namespace_uuid) DO "
          + "UPDATE SET "
          + "updated_at = EXCLUDED.updated_at, "
          + "type = EXCLUDED.type, "
          + "description = EXCLUDED.description, "
          + "job_context_template_uuid = EXCLUDED.job_context_template_uuid, "
          + "location_template = EXCLUDED.location_template, "
          + "inputs_template = EXCLUDED.inputs_template, "
          + "outputs_template = EXCLUDED.outputs_template "
          + "RETURNING *")
  JobRow upsert(
      UUID uuid,
      JobType type,
      Instant now,
      UUID namespaceUuid,
      String namespaceName,
      String name,
      String description,
      UUID jobContextUuid,
      String location,
      PGobject inputs,
      PGobject outputs);
}
