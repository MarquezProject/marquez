/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultProducers;

/**
 * This migration is dependent on the migration script in V44__runs_job_versions_add_job_uuid.sql.
 * The version returned by this code is intentionally after V44, but before V45 so that casual
 * contributors won't accidentally create a migration script that conflicts with the version of this
 * one.
 *
 * <p>This migration backfills parent jobs and runs for Airflow DAGs that reported ParentRunFacets
 * that used the task name as the job name and the scheduled run name as the runId. For example,
 * many Airflow tasks report the ParentRunFacet as <code>
 *   "parentRun": {
 *         "job": {
 *           "name": "simple_shortrunning_dag.tasks__1_of_3",
 *           "namespace": "abcdefg"
 *         },
 *         "run": {
 *           "runId": "scheduled__2022-03-14T01:40:10+00:00"
 *         }
 *       }
 * </code> The RunId can't be mapped to an existing Marquez run and the job name is actually a
 * concatenation of the DAG name and the task (which is the name assigned to the job for the task
 * itself).
 *
 * <p>This migration aims to create a real parent run, with a distinct run id, and parent job for
 * each such run by constructing a deterministic run id from the DAG name and its runId. From each
 * run, a job is created with the name field set to the DAG name.
 */
@Slf4j
public class V44_2__BackfillAirflowParentRuns implements JavaMigration {

  /**
   * Return a numeric version that is greater than 44 (so it executes after that one) but before 45
   */
  public static final MigrationVersion MIGRATION_VERSION = MigrationVersion.fromVersion("44.2");

  private static final String FIND_AIRFLOW_PARENT_RUNS_SQL =
      """
      SELECT DISTINCT(run_uuid) AS run_uuid,
                     e.parent_run_id,
                     e.parent_job_name,
                     e.parent_job_namespace
      FROM runs r
      LEFT JOIN LATERAL (
          SELECT run_uuid,
                 event->'run'->'facets'->'parent'->'run'->>'runId' AS parent_run_id,
                 event->'run'->'facets'->'parent'->'job'->>'name' AS parent_job_name,
                 event->'run'->'facets'->'parent'->'job'->>'namespace' AS parent_job_namespace
          FROM lineage_events le
          WHERE le.run_uuid=r.uuid
          AND event->'run'->'facets'->'parent'->'run'->>'runId' IS NOT NULL
          AND event->'run'->'facets'->>'airflow_version' IS NOT NULL
          ) e ON e.run_uuid=r.uuid
      WHERE e.parent_run_id IS NOT NULL
""";
  public static final String INSERT_PARENT_JOB_QUERY =
      """
      INSERT INTO jobs (uuid, type, created_at, updated_at, namespace_uuid, name, description,
      namespace_name, current_location)
      SELECT :uuid, type, created_at, updated_at, namespace_uuid, :name, description, namespace_name,
      current_location
      FROM jobs
      WHERE namespace_name=:namespace AND name=:jobName
      ON CONFLICT (name, namespace_uuid) WHERE parent_job_uuid IS NULL
      DO UPDATE SET updated_at=EXCLUDED.updated_at
      RETURNING uuid
      """;
  public static final String INSERT_PARENT_RUN_QUERY =
      """
      INSERT INTO runs (uuid, created_at, updated_at, current_run_state, external_id, namespace_name, job_name, job_uuid, location, transitioned_at, started_at, ended_at)
      SELECT :parentRunUuid, created_at, updated_at, current_run_state, :externalRunid, :namespace, :jobName, :parentJobUuid, location, transitioned_at, started_at, ended_at
      FROM runs
      WHERE uuid=:runUuid
      ON CONFLICT (uuid) DO NOTHING
      """;

  @Override
  public void migrate(Context context) throws Exception {
    Jdbi jdbi = Jdbi.create(context.getConnection());
    List<ParentRun> parentRuns =
        jdbi.withHandle(
            h ->
                h.createQuery(FIND_AIRFLOW_PARENT_RUNS_SQL)
                    .map(
                        rs -> {
                          String parentRunId = rs.getColumn("parent_run_id", String.class);
                          if (parentRunId == null) {
                            return null;
                          }
                          String parentJobName = rs.getColumn("parent_job_name", String.class);
                          String dagName =
                              parentJobName.contains(".")
                                  ? parentJobName.substring(0, parentJobName.lastIndexOf('.'))
                                  : parentJobName;
                          String parentJobNamespace =
                              rs.getColumn("parent_job_namespace", String.class);
                          String runUuid = rs.getColumn("run_uuid", String.class);
                          log.info(
                              "Found likely airflow run {} with parent {}.{} run {}",
                              runUuid,
                              parentJobNamespace,
                              parentJobName,
                              parentRunId);
                          UUID parentRunUuid;
                          try {
                            parentRunUuid = UUID.fromString(parentRunId);
                          } catch (IllegalArgumentException e) {
                            parentRunUuid = Utils.toNameBasedUuid(dagName, parentRunId);
                          }

                          return new ParentRun(
                              UUID.fromString(runUuid),
                              dagName,
                              parentJobName,
                              parentJobNamespace,
                              parentRunId,
                              parentRunUuid);
                        })
                    .list());
    parentRuns.forEach(
        parent -> {
          UUID parentJobUuid =
              jdbi.withHandle(
                  h ->
                      h.createQuery(INSERT_PARENT_JOB_QUERY)
                          .bind("uuid", UUID.randomUUID())
                          .bind("name", parent.dagName())
                          .bind("namespace", parent.namespace())
                          .bind("jobName", parent.jobName())
                          .execute(ResultProducers.returningGeneratedKeys())
                          .map((rs, ctx) -> UUID.fromString(rs.getString(1)))
                          .first());
          jdbi.withHandle(
              h ->
                  h.createQuery(INSERT_PARENT_RUN_QUERY)
                      .bind("parentRunUuid", parent.parentRunId())
                      .bind("externalRunid", parent.externalParentRunId())
                      .bind("namespace", parent.namespace())
                      .bind("jobName", parent.jobName())
                      .bind("parentJobUuid", parentJobUuid)
                      .bind("runUuid", parent.runUuid())
                      .execute(ResultProducers.returningUpdateCount()));
        });
  }

  private record ParentRun(
      UUID runUuid,
      String dagName,
      String jobName,
      String namespace,
      String externalParentRunId,
      UUID parentRunId) {}

  @Override
  public MigrationVersion getVersion() {
    return MIGRATION_VERSION;
  }

  @Override
  public String getDescription() {
    return "BackfillAirflowParentRuns";
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
