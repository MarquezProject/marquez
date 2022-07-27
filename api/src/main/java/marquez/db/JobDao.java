/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
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
import marquez.common.models.DatasetName;
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
      "SELECT EXISTS (SELECT 1 FROM jobs_view AS j "
          + "WHERE j.namespace_name= :namespaceName AND "
          + " j.name = :jobName)")
  boolean exists(String namespaceName, String jobName);

  @SqlUpdate(
      "UPDATE jobs "
          + "SET updated_at = :updatedAt, "
          + "    current_version_uuid = :currentVersionUuid "
          + "WHERE uuid = :rowUuid")
  void updateVersionFor(UUID rowUuid, Instant updatedAt, UUID currentVersionUuid);

  @SqlQuery(
      """
          SELECT j.*, jc.context, f.facets
          FROM jobs_view j
          LEFT OUTER JOIN job_versions AS jv ON jv.uuid = j.current_version_uuid
          LEFT OUTER JOIN job_contexts jc ON jc.uuid = j.current_job_context_uuid
          LEFT OUTER JOIN (
            SELECT run_uuid, JSON_AGG(e.facets) AS facets
            FROM (
              SELECT run_uuid, event->'job'->'facets' AS facets
              FROM lineage_events AS le
              INNER JOIN job_versions jv2 ON jv2.latest_run_uuid=le.run_uuid
              INNER JOIN jobs_view j2 ON j2.current_version_uuid=jv2.uuid
              WHERE j2.name=:jobName AND j2.namespace_name=:namespaceName
              ORDER BY event_time ASC
            ) e
            GROUP BY e.run_uuid
          ) f ON f.run_uuid=jv.latest_run_uuid
          WHERE j.namespace_name=:namespaceName AND (j.name=:jobName OR :jobName = ANY(j.aliases))
          AND j.symlink_target_uuid IS NULL
          """)
  Optional<Job> findJobByName(String namespaceName, String jobName);

  default Optional<Job> findWithRun(String namespaceName, String jobName) {
    Optional<Job> job = findJobByName(namespaceName, jobName);
    job.ifPresent(
        j -> {
          Optional<Run> run = createRunDao().findByLatestJob(namespaceName, jobName);
          run.ifPresent(r -> this.setJobData(r, j));
        });
    return job;
  }

  @SqlQuery(
      """
          SELECT j.*, n.name AS namespace_name
          FROM jobs_view AS j
          INNER JOIN namespaces AS n ON j.namespace_uuid = n.uuid
          WHERE j.uuid=:jobUuid
          """)
  Optional<JobRow> findJobByUuidAsRow(UUID jobUuid);

  @SqlQuery(
      """
          SELECT j.*, n.name AS namespace_name
          FROM jobs_view AS j
          INNER JOIN namespaces AS n ON j.namespace_uuid = n.uuid
          WHERE j.namespace_name=:namespaceName AND
          (j.name=:jobName OR :jobName = ANY(j.aliases))
          AND j.symlink_target_uuid IS NULL
          """)
  Optional<JobRow> findJobByNameAsRow(String namespaceName, String jobName);

  @SqlQuery(
      "SELECT j.*, jc.context, f.facets\n"
          + "  FROM jobs_view AS j\n"
          + "  LEFT OUTER JOIN job_versions AS jv ON jv.uuid = j.current_version_uuid\n"
          + "  LEFT OUTER JOIN job_contexts jc ON jc.uuid = j.current_job_context_uuid\n"
          + "LEFT OUTER JOIN (\n"
          + "      SELECT run_uuid, JSON_AGG(e.facets) AS facets\n"
          + "      FROM (\n"
          + "       SELECT run_uuid, event->'job'->'facets' AS facets\n"
          + "       FROM lineage_events AS le\n"
          + "       INNER JOIN job_versions jv2 ON jv2.latest_run_uuid=le.run_uuid\n"
          + "       INNER JOIN jobs_view j2 ON j2.current_version_uuid=jv2.uuid\n"
          + "       WHERE j2.namespace_name=:namespaceName\n"
          + "       ORDER BY event_time ASC\n"
          + "   ) e\n"
          + "    GROUP BY e.run_uuid\n"
          + "  ) f ON f.run_uuid=jv.latest_run_uuid\n"
          + "WHERE j.namespace_name = :namespaceName\n"
          + "AND j.symlink_target_uuid IS NULL\n"
          + "ORDER BY j.name "
          + "LIMIT :limit OFFSET :offset")
  List<Job> findAll(String namespaceName, int limit, int offset);

  @SqlQuery("SELECT count(*) FROM jobs_view AS j WHERE symlink_target_uuid IS NULL")
  int count();

  @SqlQuery(
      "SELECT count(*) FROM jobs_view AS j WHERE j.namespace_name = :namespaceName\n"
          + "AND symlink_target_uuid IS NULL")
  int countFor(String namespaceName);

  default List<Job> findAllWithRun(String namespaceName, int limit, int offset) {
    RunDao runDao = createRunDao();
    return findAll(namespaceName, limit, offset).stream()
        .peek(
            j ->
                runDao
                    .findByLatestJob(namespaceName, j.getName().getValue())
                    .ifPresent(run -> this.setJobData(run, j)))
        .collect(Collectors.toList());
  }

  default void setJobData(Run run, Job j) {
    j.setLatestRun(run);
    DatasetVersionDao datasetVersionDao = createDatasetVersionDao();
    j.setInputs(
        datasetVersionDao.findInputDatasetVersionsFor(run.getId().getValue()).stream()
            .map(
                ds ->
                    new DatasetId(
                        NamespaceName.of(ds.getNamespaceName()),
                        DatasetName.of(ds.getDatasetName())))
            .collect(Collectors.toSet()));
    j.setOutputs(
        datasetVersionDao.findOutputDatasetVersionsFor(run.getId().getValue()).stream()
            .map(
                ds ->
                    new DatasetId(
                        NamespaceName.of(ds.getNamespaceName()),
                        DatasetName.of(ds.getDatasetName())))
            .collect(Collectors.toSet()));
  }

  default JobRow upsertJobMeta(
      NamespaceName namespaceName, JobName jobName, JobMeta jobMeta, ObjectMapper mapper) {
    return upsertJobMeta(namespaceName, jobName, null, jobMeta, mapper);
  }

  default JobRow upsertJobMeta(
      NamespaceName namespaceName,
      JobName jobName,
      UUID symlinkTargetUuid,
      JobMeta jobMeta,
      ObjectMapper mapper) {
    Instant createdAt = Instant.now();
    NamespaceRow namespace =
        createNamespaceDao()
            .upsertNamespaceRow(
                UUID.randomUUID(), createdAt, namespaceName.getValue(), DEFAULT_NAMESPACE_OWNER);
    JobContextRow contextRow =
        createJobContextDao()
            .upsert(
                UUID.randomUUID(),
                createdAt,
                Utils.toJson(jobMeta.getContext()),
                Utils.checksumFor(jobMeta.getContext()));
    return upsertJob(
        UUID.randomUUID(),
        jobMeta.getType(),
        createdAt,
        namespace.getUuid(),
        namespace.getName(),
        jobName.getValue(),
        jobMeta.getDescription().orElse(null),
        contextRow.getUuid(),
        toUrlString(jobMeta.getLocation().orElse(null)),
        symlinkTargetUuid,
        toJson(jobMeta.getInputs(), mapper));
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
      """
          INSERT INTO jobs_view AS j (
          uuid,
          type,
          created_at,
          updated_at,
          namespace_uuid,
          namespace_name,
          name,
          description,
          current_job_context_uuid,
          current_location,
          current_inputs,
          symlink_target_uuid,
          parent_job_uuid_string
          ) VALUES (
          :uuid,
          :type,
          :now,
          :now,
          :namespaceUuid,
          :namespaceName,
          :name,
          :description,
          :jobContextUuid,
          :location,
          :inputs,
          :symlinkTargetId,
          ''
          ) RETURNING *
          """)
  JobRow upsertJob(
      UUID uuid,
      JobType type,
      Instant now,
      UUID namespaceUuid,
      String namespaceName,
      String name,
      String description,
      UUID jobContextUuid,
      String location,
      UUID symlinkTargetId,
      PGobject inputs);

  @SqlQuery(
      """
          INSERT INTO jobs_view AS j (
          uuid,
          parent_job_uuid,
          parent_job_uuid_string,
          type,
          created_at,
          updated_at,
          namespace_uuid,
          namespace_name,
          name,
          description,
          current_job_context_uuid,
          current_location,
          current_inputs,
          symlink_target_uuid
          ) VALUES (
          :uuid,
          :parentJobUuid,
          COALESCE(:parentJobUuid::text, ''),
          :type,
          :now,
          :now,
          :namespaceUuid,
          :namespaceName,
          :name,
          :description,
          :jobContextUuid,
          :location,
          :inputs,
          :symlinkTargetId
          )
          RETURNING *
          """)
  JobRow upsertJob(
      UUID uuid,
      UUID parentJobUuid,
      JobType type,
      Instant now,
      UUID namespaceUuid,
      String namespaceName,
      String name,
      String description,
      UUID jobContextUuid,
      String location,
      UUID symlinkTargetId,
      PGobject inputs);
}
