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
import lombok.NonNull;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.mappers.DatasetDataMapper;
import marquez.db.mappers.DatasetVersionDataMapper;
import marquez.db.mappers.JobDataMapper;
import marquez.db.mappers.JobRowMapper;
import marquez.db.mappers.RunDataMapper;
import marquez.db.mappers.RunMapper;
import marquez.db.mappers.UpstreamRunRowMapper;
import marquez.service.models.DatasetData;
import marquez.service.models.DatasetVersionData;
import marquez.service.models.JobData;
import marquez.service.models.Run;
import marquez.service.models.RunData;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(DatasetDataMapper.class)
@RegisterRowMapper(JobDataMapper.class)
@RegisterRowMapper(RunMapper.class)
@RegisterRowMapper(JobRowMapper.class)
@RegisterRowMapper(UpstreamRunRowMapper.class)
@RegisterRowMapper(RunDataMapper.class)
@RegisterRowMapper(DatasetVersionDataMapper.class)
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
  List<UpstreamRunRow> getUpstreamRuns(@NonNull UUID runId, int depth);

  @SqlQuery(
      """
WITH RECURSIVE
  lineage AS (
    SELECT
      r.run_uuid, r.namespace_name, r.job_name, r.state, r.created_at, r.updated_at,
      r.started_at, r.ended_at, r.job_uuid, r.job_version_uuid, r.input_version_uuid,
      r.input_dataset_uuid, r.output_version_uuid, r.output_dataset_uuid,
      r.input_dataset_namespace, r.input_dataset_name, r.input_dataset_version,
      r.input_dataset_version_uuid, r.output_dataset_namespace, r.output_dataset_name,
      r.output_dataset_version, r.output_dataset_version_uuid, r.uuid, r.parent_run_uuid,
      rf.facet as facets,
      0 AS depth
    FROM run_lineage_denormalized r
    LEFT JOIN run_facets rf ON rf.run_uuid = r.uuid
    WHERE r.run_uuid IN (<runIds>)

    UNION ALL

    SELECT
      io.run_uuid, io.namespace_name, io.job_name, io.state, io.created_at, io.updated_at,
      io.started_at, io.ended_at, io.job_uuid, io.job_version_uuid, io.input_version_uuid,
      io.input_dataset_uuid, io.output_version_uuid, io.output_dataset_uuid,
      io.input_dataset_namespace, io.input_dataset_name, io.input_dataset_version,
      io.input_dataset_version_uuid, io.output_dataset_namespace, io.output_dataset_name,
      io.output_dataset_version, io.output_dataset_version_uuid, io.uuid, io.parent_run_uuid,
      rf.facet as facets,
      l.depth + 1 AS depth
    FROM run_lineage_denormalized io
    LEFT JOIN run_facets rf ON rf.run_uuid = io.uuid
    JOIN lineage l
      ON (io.input_version_uuid = l.output_version_uuid OR io.output_version_uuid = l.input_version_uuid)
     AND io.run_uuid != l.run_uuid
    WHERE l.depth < :depth
  )
SELECT
  run_uuid AS uuid,
  created_at,
  updated_at,
  started_at,
  ended_at,
  state,
  job_uuid,
  job_version_uuid,
  namespace_name,
  job_name,
  COALESCE(ARRAY_AGG(DISTINCT input_dataset_uuid) FILTER (WHERE input_dataset_uuid IS NOT NULL), Array[]::uuid[]) AS input_uuids,
  COALESCE(ARRAY_AGG(DISTINCT output_dataset_uuid) FILTER (WHERE output_dataset_uuid IS NOT NULL), Array[]::uuid[]) AS output_uuids,
  JSON_AGG(DISTINCT jsonb_build_object('namespace', input_dataset_namespace,
              'name', input_dataset_name,
              'version', input_dataset_version,
              'dataset_version_uuid', input_dataset_version_uuid)) FILTER (WHERE input_dataset_name IS NOT NULL) AS input_versions,
  JSON_AGG(DISTINCT jsonb_build_object('namespace', output_dataset_namespace,
                                                      'name', output_dataset_name,
                                                      'version', output_dataset_version,
                                                      'dataset_version_uuid', output_dataset_version_uuid
                                                      )) FILTER (WHERE output_dataset_name IS NOT NULL) AS output_versions,
  COALESCE(Array_AGG(distinct uuid) FILTER (WHERE uuid IS NOT NULL), Array[]::uuid[]) as child_run_id,
  COALESCE(Array_AGG(distinct parent_run_uuid) FILTER (WHERE parent_run_uuid IS NOT NULL), Array[]::uuid[]) as parent_run_id,
  JSON_AGG(DISTINCT lineage.facets) as facets,
  MIN(depth) AS depth
FROM lineage
GROUP BY
  run_uuid, created_at, updated_at, started_at, ended_at,
  state, job_uuid, job_version_uuid, namespace_name, job_name
""")
  Set<RunData> getRunLineage(@BindList("runIds") Set<UUID> runIds, @Bind("depth") int depth);

  @SqlQuery(
      """
      SELECT EXISTS (
          SELECT 1 FROM runs
          WHERE parent_run_uuid IN (<runIds>)
      )
      """)
  boolean hasChildRuns(@BindList("runIds") Set<UUID> runIds);

  @SqlQuery("""
      SELECT parent_run_uuid FROM runs
      WHERE uuid = :runId
      """)
  Optional<UUID> getParentRunUuid(@Bind("runId") UUID runId);

  public record RunLineageRow(
      UUID runUuid,
      UUID datasetUuid,
      UUID datasetVersion,
      String datasetNamespace,
      String datasetName,
      UUID producerRunUuid,
      String edgeType,
      int depth,
      Instant startedAt,
      Instant endedAt,
      String state,
      UUID jobUuid,
      UUID jobVersionUuid,
      String jobNamespace,
      String jobName) {}

  @SqlQuery(
      """
     WITH RECURSIVE
      lineage AS (
        SELECT
          r.run_uuid, r.namespace_name, r.job_name, r.state, r.created_at, r.updated_at,
          r.started_at, r.ended_at, r.job_uuid, r.job_version_uuid, r.input_version_uuid,
          r.input_dataset_uuid, r.output_version_uuid, r.output_dataset_uuid,
          r.input_dataset_namespace, r.input_dataset_name, r.input_dataset_version,
          r.input_dataset_version_uuid, r.output_dataset_namespace, r.output_dataset_name,
          r.output_dataset_version, r.output_dataset_version_uuid, r.uuid, r.parent_run_uuid,
          rf.facet as facets,
          0 AS depth
        FROM run_parent_lineage_denormalized r
        LEFT JOIN run_facets rf ON rf.run_uuid = r.uuid
        WHERE r.run_uuid IN (<runIds>)

        UNION ALL

        SELECT
          io.run_uuid, io.namespace_name, io.job_name, io.state, io.created_at, io.updated_at,
          io.started_at, io.ended_at, io.job_uuid, io.job_version_uuid, io.input_version_uuid,
          io.input_dataset_uuid, io.output_version_uuid, io.output_dataset_uuid,
          io.input_dataset_namespace, io.input_dataset_name, io.input_dataset_version,
          io.input_dataset_version_uuid, io.output_dataset_namespace, io.output_dataset_name,
          io.output_dataset_version, io.output_dataset_version_uuid, io.uuid, io.parent_run_uuid,
          rf.facet as facets,
          l.depth + 1 AS depth
        FROM run_parent_lineage_denormalized io
        LEFT JOIN run_facets rf ON rf.run_uuid = io.uuid
        JOIN lineage l
          ON (io.input_version_uuid = l.output_version_uuid OR io.output_version_uuid = l.input_version_uuid)
         AND io.run_uuid != l.run_uuid
        WHERE l.depth < :depth
      )
    SELECT
      run_uuid AS uuid,
      created_at,
      updated_at,
      started_at,
      ended_at,
      state,
      job_uuid,
      job_version_uuid,
      namespace_name,
      job_name,
      COALESCE(ARRAY_AGG(DISTINCT input_dataset_uuid) FILTER (WHERE input_dataset_uuid IS NOT NULL), Array[]::uuid[]) AS input_uuids,
      COALESCE(ARRAY_AGG(DISTINCT output_dataset_uuid) FILTER (WHERE output_dataset_uuid IS NOT NULL), Array[]::uuid[]) AS output_uuids,
	  JSON_AGG(DISTINCT jsonb_build_object('namespace', input_dataset_namespace,
              'name', input_dataset_name,
              'version', input_dataset_version,
              'dataset_version_uuid', input_dataset_version_uuid)) FILTER (WHERE input_dataset_name IS NOT NULL) AS input_versions,
	  JSON_AGG(DISTINCT jsonb_build_object('namespace', output_dataset_namespace,
                                                      'name', output_dataset_name,
                                                      'version', output_dataset_version,
                                                      'dataset_version_uuid', output_dataset_version_uuid
                                                      )) FILTER (WHERE output_dataset_name IS NOT NULL) AS output_versions,
	COALESCE(Array_AGG(distinct uuid), Array[]::uuid[]) as child_run_id,
  COALESCE(Array_AGG(distinct parent_run_uuid), Array[]::uuid[]) as parent_run_id,
  JSON_AGG(DISTINCT lineage.facets) as facets,
      MIN(depth) AS depth
    FROM lineage
    GROUP BY
      run_uuid, created_at, updated_at, started_at, ended_at,
      state, job_uuid, job_version_uuid, namespace_name, job_name
    """)
  Set<RunData> getParentRunLineage(
      @BindList(value = "runIds", onEmpty = BindList.EmptyHandling.NULL_STRING) Set<UUID> runIds,
      @Bind("depth") int depth);

  @SqlQuery(
      """
      WITH selected_dataset_versions AS (
          SELECT dv.*
          FROM dataset_versions dv
          WHERE dv.uuid IN (<versions>)
      ), selected_dataset_version_facets AS (
          SELECT dv.uuid, dv.dataset_name, dv.namespace_name, df.run_uuid, df.lineage_event_time, df.facet
          FROM selected_dataset_versions dv
          LEFT JOIN dataset_facets_view df ON df.dataset_version_uuid = dv.uuid
      )
      SELECT dv.uuid,d.type, d.name, d.physical_name, d.namespace_name, d.source_name, d.description, dv.lifecycle_state,
          dv.created_at, dv.uuid AS current_version_uuid, dv.version, dv.dataset_schema_version_uuid, dv.fields, dv.run_uuid AS createdByRunUuid,
		  rp.parent_run_uuid as createdByParentRunUuid,
          sv.schema_location, t.tags, f.facets
      FROM selected_dataset_versions dv
      LEFT JOIN datasets_view d ON d.uuid = dv.dataset_uuid
      LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid
	  LEFT JOIN runs AS rp ON rp.uuid = dv.run_uuid
      LEFT JOIN (
          SELECT ARRAY_AGG(t.name) AS tags, m.dataset_uuid
          FROM tags AS t
                   INNER JOIN datasets_tag_mapping AS m ON m.tag_uuid = t.uuid
          GROUP BY m.dataset_uuid
      ) t ON t.dataset_uuid = dv.dataset_uuid
      LEFT JOIN (
          SELECT dvf.uuid AS dataset_uuid, JSONB_AGG(dvf.facet ORDER BY dvf.lineage_event_time ASC) AS facets
          FROM selected_dataset_version_facets dvf
          WHERE dvf.run_uuid = dvf.run_uuid
          GROUP BY dvf.uuid
      ) f ON f.dataset_uuid = dv.uuid""")
  Set<DatasetVersionData> getDatasetVersionData(
      @BindList(value = "versions", onEmpty = BindList.EmptyHandling.NULL_STRING)
          Set<UUID> versions);
}
