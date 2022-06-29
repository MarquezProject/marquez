/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.service.models.LineageEvent.ParentRunFacet;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultProducers;

@Slf4j
public class V44_3_BackfillJobsWithParents implements JavaMigration {

  public static final String FIND_JOBS_WITH_PARENT_RUNS =
      """
      SELECT j.uuid AS job_uuid, p.parent
      FROM jobs j
      LEFT JOIN LATERAL(
        SELECT uuid AS run_uuid FROM runs
        WHERE job_name=j.name AND namespace_name=j.namespace_name
        ORDER BY transitioned_at DESC
        LIMIT 1
      ) r ON true
      LEFT JOIN LATERAL (
        SELECT event->'run'->'facets'->'parent' parent FROM lineage_events
        WHERE run_uuid=r.run_uuid
        AND event->'run'->'facets'->'parent' IS NOT NULL
        ORDER BY event_time DESC
        LIMIT 1
      ) p ON true
      WHERE p.parent IS NOT NULL
      """;

  public static final String FIND_JOB_UUID_FOR_RUN =
      """
      SELECT job_uuid FROM runs WHERE uuid=:uuid AND job_uuid IS NOT NULL
      """;
  public static final String INSERT_NEW_JOB_WITH_PARENT =
      """
      INSERT INTO jobs(uuid, type, created_at, updated_at, namespace_uuid, name, description,
        current_version_uuid, namespace_name, current_job_context_uuid, current_location, current_inputs,
        parent_job_uuid)
      SELECT :uuid, type, created_at, updated_at, namespace_uuid, name, description, current_version_uuid,
        namespace_name, current_job_context_uuid, current_location, current_inputs, :parent_job_uuid
        FROM jobs
        WHERE uuid=:job_uuid
        ON CONFLICT (name, namespace_name, parent_job_uuid) DO NOTHING
        RETURNING uuid
      """;
  public static final String SYMLINK_OLD_JOB_TO_NEW =
      "UPDATE jobs SET symlink_target_uuid=:target_uuid WHERE uuid=:job_uuid";

  @Override
  public MigrationVersion getVersion() {
    return MigrationVersion.fromVersion("44.3");
  }

  @Override
  public void migrate(Context context) throws Exception {
    Jdbi jdbi = Jdbi.create(context.getConnection());
    List<JobParent> jobParents =
        jdbi.withHandle(
            h ->
                h
                    .createQuery(FIND_JOBS_WITH_PARENT_RUNS)
                    .map(
                        (rs, ctx) -> {
                          Optional<UUID> parentRunUuid = findParentRunIdForJob(rs);
                          UUID jobId = UUID.fromString(rs.getString("job_uuid"));
                          return parentRunUuid.map(runId -> new JobParent(jobId, runId));
                        })
                    .stream()
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList()));
    jobParents.forEach(
        jp -> {
          Optional<UUID> jobUuid =
              jdbi.withHandle(
                      h -> h.createQuery(FIND_JOB_UUID_FOR_RUN).bind("uuid", jp.parentRunId))
                  .map((rs, ctx) -> UUID.fromString(rs.getString("job_uuid")))
                  .findFirst();
          jobUuid
              .flatMap(
                  uuid ->
                      jdbi.withHandle(
                          h ->
                              h.createQuery(INSERT_NEW_JOB_WITH_PARENT)
                                  .bind("uuid", UUID.randomUUID())
                                  .bind("parent_job_uuid", uuid)
                                  .bind("job_uuid", jp.jobId)
                                  .execute(ResultProducers.returningGeneratedKeys("uuid"))
                                  .map(rs -> rs.getColumn("uuid", UUID.class))
                                  .findFirst()))
              .ifPresent(
                  newTargetUuid -> {
                    jdbi.withHandle(
                            h ->
                                h.createQuery(SYMLINK_OLD_JOB_TO_NEW)
                                    .bind("job_uuid", jp.jobId)
                                    .bind("target_uuid", newTargetUuid))
                        .execute(ResultProducers.returningUpdateCount());
                  });
        });
  }

  private Optional<UUID> findParentRunIdForJob(ResultSet resultSet) throws SQLException {
    String parentJson = resultSet.getString("parent");
    try {
      ParentRunFacet parentRunFacet = Utils.getMapper().readValue(parentJson, ParentRunFacet.class);
      return Optional.of(Utils.findParentRunUuid(parentRunFacet));
    } catch (JsonProcessingException e) {
      log.warn(
          "Unable to process parent run facet from event for run {}: {}",
          resultSet.getString("run_uuid"),
          parentJson);
    }
    return Optional.empty();
  }

  private record JobParent(UUID jobId, UUID parentRunId) {}

  @Override
  public String getDescription() {
    return "BackfillJobsWithParents";
  }

  @Override
  public Integer getChecksum() {
    return null;
  }

  @Override
  public boolean isUndo() {
    return false;
  }

  @Override
  public boolean canExecuteInTransaction() {
    return false;
  }

  @Override
  public boolean isBaselineMigration() {
    return false;
  }
}
