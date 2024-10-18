/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.mappers.DatasetDataMapper;
import marquez.db.mappers.JobDataMapper;
import marquez.db.mappers.JobRowMapper;
import marquez.db.mappers.RunMapper;
import marquez.db.mappers.UpstreamRunRowMapper;
import marquez.service.models.DatasetData;
import marquez.service.models.JobData;
import marquez.service.models.Run;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(DatasetDataMapper.class)
@RegisterRowMapper(JobDataMapper.class)
@RegisterRowMapper(RunMapper.class)
@RegisterRowMapper(JobRowMapper.class)
@RegisterRowMapper(UpstreamRunRowMapper.class)
public interface LineageDao {

  public record JobSummary(NamespaceName namespace, JobName name, UUID version) {}

  public record RunSummary(RunId id, Instant start, Instant end, String status) {}

  public record DatasetSummary(
      NamespaceName namespace, DatasetName name, UUID version, RunId producedByRunId) {}

  public record UpstreamRunRow(JobSummary job, RunSummary run, DatasetSummary input) {}

  /**
   * Fetch all of the jobs that consume or produce the datasets that are consumed or produced by the
   * input jobIds. This returns a single layer from the BFS using datasets as edges. Jobs that have
   * no input or output datasets will have no results. Jobs that have no upstream producers or
   * downstream consumers will have the original jobIds returned.
   *
   * @param jobIds
   * @return
   */
  @SqlQuery(
      """
      WITH RECURSIVE
                 job_io AS (
                    SELECT
                           io.job_uuid AS job_uuid,
                           io.job_symlink_target_uuid AS job_symlink_target_uuid,
                           ARRAY_AGG(DISTINCT io.dataset_uuid) FILTER (WHERE io.io_type='INPUT') AS inputs,
                           ARRAY_AGG(DISTINCT io.dataset_uuid) FILTER (WHERE io.io_type='OUTPUT') AS outputs
                    FROM job_versions_io_mapping io
                    WHERE io.is_current_job_version = TRUE
                    GROUP BY io.job_symlink_target_uuid, io.job_uuid
                ),
                lineage(job_uuid, job_symlink_target_uuid, inputs, outputs) AS (
                    SELECT job_uuid,
                           job_symlink_target_uuid,
                           COALESCE(inputs, Array[]::uuid[]) AS inputs,
                           COALESCE(outputs, Array[]::uuid[]) AS outputs,
                           0 AS depth
                    FROM job_io
                    WHERE job_uuid IN (<jobIds>) OR job_symlink_target_uuid IN (<jobIds>)
                    UNION
                    SELECT io.job_uuid, io.job_symlink_target_uuid, io.inputs, io.outputs, l.depth + 1
                    FROM job_io io, lineage l
                    WHERE (io.job_uuid != l.job_uuid) AND
                        array_cat(io.inputs, io.outputs) && array_cat(l.inputs, l.outputs)
                      AND depth < :depth),
                lineage_outside_job_io(job_uuid) AS (
                    SELECT
                      param_jobs.param_job_uuid as job_uuid,
                      j.symlink_target_uuid,
                      Array[]::uuid[] AS inputs,
                      Array[]::uuid[] AS outputs,
                      0 AS depth
                    FROM (SELECT unnest(ARRAY[<jobIds>]::UUID[]) AS param_job_uuid) param_jobs
                    LEFT JOIN lineage l on param_jobs.param_job_uuid = l.job_uuid
                    INNER JOIN jobs j ON j.uuid = param_jobs.param_job_uuid
                    WHERE l.job_uuid IS NULL
                )
            SELECT DISTINCT ON (j.uuid) j.*, inputs AS input_uuids, outputs AS output_uuids
            FROM (SELECT * FROM lineage UNION SELECT * FROM lineage_outside_job_io) l2
            INNER JOIN jobs_view j ON (j.uuid=l2.job_uuid OR j.uuid=l2.job_symlink_target_uuid)
  """)
  Set<JobData> getLineage(@BindList Set<UUID> jobIds, int depth);

  @SqlQuery(
      """
    SELECT j.*, NULL as input_uuids, NULL AS output_uuids FROM jobs_view j
    WHERE j.parent_job_uuid= :jobId
    LIMIT 1""")
  Optional<JobData> getParentJobData(UUID jobId);

  @SqlQuery(
      """
      SELECT ds.*, dv.fields, dv.lifecycle_state
      FROM datasets_view ds
      LEFT JOIN dataset_versions dv ON dv.uuid = ds.current_version_uuid
      LEFT JOIN dataset_symlinks dsym ON dsym.namespace_uuid = ds.namespace_uuid and dsym.name = ds.name
      WHERE dsym.is_primary = true
      AND ds.uuid IN (<dsUuids>)""")
  Set<DatasetData> getDatasetData(@BindList Set<UUID> dsUuids);

  @SqlQuery(
      """
      SELECT ds.*, dv.fields, dv.lifecycle_state
      FROM datasets_view ds
      LEFT JOIN dataset_versions dv on dv.uuid = ds.current_version_uuid
      LEFT JOIN dataset_symlinks dsym ON dsym.namespace_uuid = ds.namespace_uuid and dsym.name = ds.name
      INNER JOIN datasets_view AS d ON d.uuid = ds.uuid
      WHERE dsym.is_primary is true
      AND CAST((:namespaceName, :datasetName) AS DATASET_NAME) = ANY(d.dataset_symlinks)""")
  DatasetData getDatasetData(String namespaceName, String datasetName);

  @SqlQuery(
      """
      SELECT j.uuid FROM jobs j
      INNER JOIN job_versions jv ON jv.job_uuid = j.uuid
      INNER JOIN job_versions_io_mapping io ON io.job_version_uuid = jv.uuid
      INNER JOIN datasets_view ds ON ds.uuid = io.dataset_uuid
      WHERE ds.name = :datasetName AND ds.namespace_name = :namespaceName
      ORDER BY io_type DESC, jv.created_at DESC
      LIMIT 1""")
  Optional<UUID> getJobFromInputOrOutput(String datasetName, String namespaceName);

  @SqlQuery(
      """
                  WITH latest_runs AS (
                      SELECT DISTINCT on(r.job_name, r.namespace_name) r.*, jv.version
                      FROM runs_view r
                      INNER JOIN job_versions jv ON jv.uuid=r.job_version_uuid
                      INNER JOIN jobs_view j ON j.uuid=jv.job_uuid
                      WHERE j.uuid in (<jobUuid>) OR j.symlink_target_uuid IN (<jobUuid>)
                      ORDER BY r.job_name, r.namespace_name, created_at DESC
                  )
                  SELECT r.*, ra.args, f.facets,
                    r.version AS job_version, ri.input_versions, ro.output_versions
                    from latest_runs AS r
                  LEFT JOIN run_args AS ra ON ra.uuid = r.run_args_uuid
                  LEFT JOIN LATERAL (
                      SELECT im.run_uuid,
                             JSON_AGG(json_build_object('namespace', dv.namespace_name,
                                                        'name', dv.dataset_name,
                                                        'version', dv.version)) AS input_versions
                      FROM runs_input_mapping im
                      INNER JOIN dataset_versions dv on im.dataset_version_uuid = dv.uuid
                      WHERE im.run_uuid=r.uuid
                      GROUP BY im.run_uuid
                  ) ri ON ri.run_uuid=r.uuid
                  LEFT JOIN LATERAL (
                      SELECT rf.run_uuid, JSON_AGG(rf.facet ORDER BY rf.lineage_event_time ASC) AS facets
                      FROM run_facets_view AS rf
                      WHERE rf.run_uuid=r.uuid
                      GROUP BY rf.run_uuid
                  ) AS f ON r.uuid=f.run_uuid
                  LEFT JOIN LATERAL (
                      SELECT run_uuid, JSON_AGG(json_build_object('namespace', namespace_name,
                                                                  'name', dataset_name,
                                                                  'version', version)) AS output_versions
                      FROM dataset_versions
                      WHERE run_uuid=r.uuid
                      GROUP BY run_uuid
                  ) ro ON ro.run_uuid=r.uuid
                  """)
  List<Run> getCurrentRunsWithFacets(@BindList Collection<UUID> jobUuid);

  @SqlQuery(
      """
        WITH latest_runs AS (SELECT current_run_uuid, current_version_uuid AS job_version
                             FROM jobs j
                             WHERE j.uuid in (<jobUuid>) OR j.symlink_target_uuid IN (<jobUuid>))
        SELECT *
        FROM runs
                 inner join latest_runs ON runs.uuid = latest_runs.current_run_uuid
        ORDER BY runs.created_at desc;
      """)
  List<Run> getCurrentRuns(@BindList Collection<UUID> jobUuid);

  @SqlQuery(
      """
      WITH RECURSIVE
        upstream_runs(
                r_uuid, -- run uuid
                dataset_uuid, dataset_version_uuid, dataset_namespace, dataset_name, -- input dataset version to the run
                u_r_uuid, -- upstream run that produced that dataset version
                depth -- current depth of traversal
                ) AS (

          -- initial case: find the inputs of the initial runs
          select r.uuid,
                 dv.dataset_uuid, dv."version", dv.namespace_name, dv.dataset_name,
                 dv.run_uuid,
                 0 AS depth -- starts at 0
          FROM (SELECT :runId::uuid AS uuid) r -- initial run
          LEFT JOIN runs_input_mapping rim ON rim.run_uuid = r.uuid
          LEFT JOIN dataset_versions dv ON dv.uuid = rim.dataset_version_uuid

        UNION

          -- recursion: find the inputs of the inputs found on the previous iteration and increase depth to know when to stop
          SELECT
                ur.u_r_uuid,
                dv2.dataset_uuid, dv2."version", dv2.namespace_name, dv2.dataset_name,
                dv2.run_uuid,
                ur.depth + 1 AS depth -- increase depth to check end condition
          FROM upstream_runs ur
          LEFT JOIN runs_input_mapping rim2 ON rim2.run_uuid = ur.u_r_uuid
          LEFT JOIN dataset_versions dv2 ON dv2.uuid = rim2.dataset_version_uuid
          -- end condition of the recursion: no input or depth is over the maximum set
          -- also avoid following cycles (ex: merge statement)
          WHERE ur.u_r_uuid IS NOT NULL AND ur.u_r_uuid <> ur.r_uuid AND depth < :depth
        )

      -- present the result: use Distinct as we may have traversed the same edge multiple times if there are diamonds in the graph.
      SELECT * FROM ( -- we need the extra statement to sort after the DISTINCT
          SELECT DISTINCT ON (upstream_runs.r_uuid, upstream_runs.dataset_version_uuid, upstream_runs.u_r_uuid)
            upstream_runs.*,
            r.started_at, r.ended_at, r.current_run_state as state,
            r.job_uuid, r.job_version_uuid, r.namespace_name as job_namespace, r.job_name
          FROM upstream_runs, runs r WHERE upstream_runs.r_uuid = r.uuid
        ) sub
      ORDER BY depth ASC, job_name ASC;
      """)
  List<UpstreamRunRow> getUpstreamRuns(@NotNull UUID runId, int depth);
}
