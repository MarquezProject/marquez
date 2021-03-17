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
import java.util.stream.Collectors;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.db.mappers.JobMapper;
import marquez.db.mappers.JobRowMapper;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;

@RegisterRowMapper(JobRowMapper.class)
@RegisterRowMapper(JobMapper.class)
public interface JobDao extends BaseDao {
  @SqlQuery(
      "SELECT EXISTS (SELECT 1 FROM jobs AS j "
          + "WHERE j.namespace_name= :namespaceName AND "
          + " j.name = :jobName)")
  boolean exists(String namespaceName, String jobName);

  @SqlUpdate(
      "UPDATE jobs "
          + "SET updated_at = :updatedAt, "
          + "    current_version_uuid = :currentVersionUuid "
          + "WHERE uuid = :rowUuid")
  void updateVersion(UUID rowUuid, Instant updatedAt, UUID currentVersionUuid);

  String JOB_SELECT =
      "SELECT j.*, jc.context "
          + "FROM jobs AS j "
          + "left outer join job_contexts jc on jc.uuid = j.current_job_context_uuid ";

  @SqlQuery(JOB_SELECT + "WHERE j.namespace_name = :namespaceName AND " + "      j.name = :jobName")
  Optional<Job> find(String namespaceName, String jobName);

  default Optional<Job> findWithRun(String namespaceName, String jobName) {
    Optional<Job> job = find(namespaceName, jobName);
    job.ifPresent(
        j -> {
          Optional<Run> run = createRunDao().findByLatestJob(namespaceName, jobName);
          run.ifPresent(j::setLatestRun);
        });
    return job;
  }

  @SqlQuery(
      "SELECT j.*, n.name AS namespace_name FROM jobs AS j "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.name = :namespaceName AND "
          + "      j.namespace_uuid = n.uuid AND "
          + "      j.name = :jobName)")
  Optional<JobRow> findByRow(String namespaceName, String jobName);

  @SqlQuery(
      JOB_SELECT
          + "WHERE namespace_name = :namespaceName "
          + "ORDER BY j.name "
          + "LIMIT :limit OFFSET :offset")
  List<Job> findAll(String namespaceName, int limit, int offset);

  default List<Job> findAllWithRun(String namespaceName, int limit, int offset) {
    RunDao runDao = createRunDao();
    return findAll(namespaceName, limit, offset).stream()
        .peek(
            j ->
                runDao
                    .findByLatestJob(namespaceName, j.getName().getValue())
                    .ifPresent(j::setLatestRun))
        .collect(Collectors.toList());
  }

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
          + "current_job_context_uuid,"
          + "current_location,"
          + "current_inputs,"
          + "current_outputs "
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
          + "current_job_context_uuid = EXCLUDED.current_job_context_uuid, "
          + "current_location = EXCLUDED.current_location, "
          + "current_inputs = EXCLUDED.current_inputs, "
          + "current_outputs = EXCLUDED.current_outputs "
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
