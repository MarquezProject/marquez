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

import static com.google.common.base.Charsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static marquez.common.Utils.KV_JOINER;
import static marquez.common.Utils.VERSION_DELIM;
import static marquez.common.Utils.VERSION_JOINER;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Value;
import marquez.common.Utils;
import marquez.db.mappers.ExtendedJobVersionRowMapper;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(ExtendedJobVersionRowMapper.class)
public interface JobVersionDao extends MarquezDao {
  enum IoType {
    INPUT,
    OUTPUT;
  }

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM job_versions WHERE version = :version)")
  boolean exists(UUID version);

  @SqlUpdate(
      "UPDATE job_versions "
          + "SET updated_at = :updatedAt, "
          + "    latest_run_uuid = :latestRunUuid "
          + "WHERE uuid = :rowUuid")
  void updateLatestRun(UUID rowUuid, Instant updatedAt, UUID latestRunUuid);

  @SqlQuery(
      "INSERT INTO job_versions ("
          + "uuid, "
          + "created_at, "
          + "updated_at, "
          + "job_uuid, "
          + "job_context_uuid, "
          + "location,"
          + "version,"
          + "job_name,"
          + "namespace_uuid,"
          + "namespace_name"
          + ") VALUES ("
          + ":uuid, "
          + ":now, "
          + ":now, "
          + ":jobUuid, "
          + ":jobContextUuid, "
          + ":location, "
          + ":version, "
          + ":jobName, "
          + ":namespaceUuid, "
          + ":namespaceName) "
          + "ON CONFLICT(version) DO "
          + "UPDATE SET "
          + "updated_at = EXCLUDED.updated_at, "
          + "job_context_uuid = EXCLUDED.job_context_uuid "
          + "RETURNING *")
  ExtendedJobVersionRow upsert(
      UUID uuid,
      Instant now,
      UUID jobUuid,
      UUID jobContextUuid,
      String location,
      UUID version,
      String jobName,
      UUID namespaceUuid,
      String namespaceName);

  @SqlUpdate(
      "INSERT INTO job_versions_io_mapping ("
          + "job_version_uuid, dataset_uuid, io_type) "
          + "VALUES (:jobVersionUuid, :datasetUuid, :ioType) ON CONFLICT DO NOTHING")
  void upsertDatasetIoMapping(UUID jobVersionUuid, UUID datasetUuid, IoType ioType);

  default JobVersionBag createJobVersionOnComplete(
      Instant transitionedAt, UUID runUuid, String namespaceName, String jobName) {
    DatasetVersionDao datasetVersionDao = createDatasetVersionDao();
    JobVersionDao jobVersionDao = createJobVersionDao();
    JobDao jobDao = createJobDao();
    JobContextDao jobContextDao = createJobContextDao();
    JobRow jobRow = jobDao.find(namespaceName, jobName).get();
    Optional<JobContextRow> jobContextRow = jobContextDao.findBy(jobRow.getJobContextUuid());
    Map context =
        jobContextRow
            .map(e -> Utils.fromJson(e.getContext(), new TypeReference<Map<String, String>>() {}))
            .orElse(new HashMap<>());
    List<ExtendedDatasetVersionRow> inputs = datasetVersionDao.findInputsByRunId(runUuid);
    List<ExtendedDatasetVersionRow> outputs = datasetVersionDao.findByRunId(runUuid);
    NamespaceRow namespaceRow = createNamespaceDao().findBy(jobRow.getNamespaceName()).get();

    JobVersionRow jobVersion =
        jobVersionDao.upsert(
            UUID.randomUUID(),
            transitionedAt,
            jobRow.getUuid(),
            jobRow.getJobContextUuid(),
            jobRow.getLocation(),
            buildJobVersion(
                jobRow.getNamespaceName(),
                jobRow.getName(),
                inputs,
                outputs,
                context,
                jobRow.getLocation()),
            jobRow.getName(),
            namespaceRow.getUuid(),
            jobRow.getNamespaceName());

    createRunDao().updateJobVersion(runUuid, jobVersion.getUuid());

    jobDao.updateVersion(jobRow.getUuid(), transitionedAt, jobVersion.getUuid());
    jobVersionDao.updateLatestRun(jobVersion.getUuid(), transitionedAt, runUuid);
    for (ExtendedDatasetVersionRow datasetVersionRow : inputs) {
      jobVersionDao.upsertDatasetIoMapping(
          jobVersion.getUuid(), datasetVersionRow.getDatasetUuid(), IoType.INPUT);
    }

    for (ExtendedDatasetVersionRow datasetVersionRow : outputs) {
      jobVersionDao.upsertDatasetIoMapping(
          jobVersion.getUuid(), datasetVersionRow.getDatasetUuid(), IoType.OUTPUT);
    }

    return new JobVersionBag(jobRow, inputs, outputs, jobVersion);
  }

  default UUID buildJobVersion(
      String namespaceName,
      String jobName,
      List<ExtendedDatasetVersionRow> inputs,
      List<ExtendedDatasetVersionRow> outputs,
      Map context,
      String location) {
    final byte[] bytes =
        VERSION_JOINER
            .join(
                namespaceName,
                jobName,
                inputs.stream().flatMap(JobVersionDao::idToStream).collect(joining(VERSION_DELIM)),
                outputs.stream().flatMap(JobVersionDao::idToStream).collect(joining(VERSION_DELIM)),
                location,
                KV_JOINER.join(context))
            .getBytes(UTF_8);
    return UUID.nameUUIDFromBytes(bytes);
  }

  public static Stream<String> idToStream(ExtendedDatasetVersionRow dataset) {
    return Stream.of(dataset.getNamespaceName(), dataset.getDatasetName());
  }

  @Value
  class JobVersionBag {
    JobRow jobRow;
    List<ExtendedDatasetVersionRow> inputs;
    List<ExtendedDatasetVersionRow> outputs;
    JobVersionRow jobVersionRow;
  }
}
