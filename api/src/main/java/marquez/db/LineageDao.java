/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import marquez.db.mappers.DatasetDataMapper;
import marquez.db.mappers.JobDataMapper;
import marquez.db.mappers.JobRowMapper;
import marquez.db.mappers.RunMapper;
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
public interface LineageDao {

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
            SELECT COALESCE(j.symlink_target_uuid, j.uuid) AS job_uuid,
            ARRAY_AGG(DISTINCT io.dataset_uuid) FILTER (WHERE io_type='INPUT') AS inputs,
            ARRAY_AGG(DISTINCT io.dataset_uuid) FILTER (WHERE io_type='OUTPUT') AS outputs
            FROM job_versions_io_mapping io
            INNER JOIN job_versions v ON io.job_version_uuid=v.uuid
            INNER JOIN jobs_view j on j.current_version_uuid = v.uuid
            LEFT JOIN jobs_view s ON s.uuid=j.symlink_target_uuid
            WHERE s.current_version_uuid IS NULL
            GROUP BY COALESCE(j.symlink_target_uuid, j.uuid)
        ),
        lineage(job_uuid, inputs, outputs) AS (
            SELECT COALESCE(j.symlink_target_uuid, j.uuid) AS job_uuid,
                   COALESCE(inputs, Array[]::uuid[]) AS inputs,
                   COALESCE(outputs, Array[]::uuid[]) AS outputs,
                   0 AS depth
            FROM jobs_view j
            LEFT JOIN job_io io ON io.job_uuid=j.uuid OR j.symlink_target_uuid=io.job_uuid
            WHERE j.uuid IN (<jobIds>)
            UNION
            SELECT io.job_uuid, io.inputs, io.outputs, l.depth + 1
            FROM job_io io,
                 lineage l
            WHERE io.job_uuid != l.job_uuid AND
            array_cat(io.inputs, io.outputs) && array_cat(l.inputs, l.outputs)
            AND depth < :depth)
    SELECT DISTINCT ON (j.uuid) j.*, inputs AS input_uuids, outputs AS output_uuids, jc.context
    FROM lineage l2
    INNER JOIN jobs_view j ON j.uuid=l2.job_uuid
    LEFT JOIN job_contexts jc on jc.uuid = j.current_job_context_uuid;
  """)
  Set<JobData> getLineage(@BindList Set<UUID> jobIds, int depth);

  @SqlQuery(
      """
      SELECT ds.*, dv.fields, dv.lifecycle_state
      FROM datasets_view ds
      LEFT JOIN dataset_versions dv on dv.uuid = ds.current_version_uuid
      WHERE ds.uuid IN (<dsUuids>)""")
  Set<DatasetData> getDatasetData(@BindList Set<UUID> dsUuids);

  @SqlQuery(
      """
      SELECT ds.*, dv.fields, dv.lifecycle_state
      FROM datasets_view ds
      LEFT JOIN dataset_versions dv on dv.uuid = ds.current_version_uuid
      WHERE ds.name = :datasetName AND ds.namespace_name = :namespaceName""")
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
      "WITH latest_runs AS (\n"
          + "    SELECT DISTINCT on(r.job_name, r.namespace_name) r.*, jv.version\n"
          + "    FROM runs_view r\n"
          + "    INNER JOIN job_versions jv ON jv.uuid=r.job_version_uuid\n"
          + "    INNER JOIN jobs_view j ON j.uuid=jv.job_uuid\n"
          + "    WHERE j.uuid in (<jobUuid>) OR j.symlink_target_uuid IN (<jobUuid>)\n"
          + "    ORDER BY r.job_name, r.namespace_name, created_at DESC\n"
          + ")\n"
          + "SELECT r.*, ra.args, ctx.context, f.facets,\n"
          + "  r.version AS job_version, ri.input_versions, ro.output_versions\n"
          + "  from latest_runs AS r\n"
          + "LEFT JOIN run_args AS ra ON ra.uuid = r.run_args_uuid\n"
          + "LEFT JOIN job_contexts AS ctx ON r.job_context_uuid = ctx.uuid\n"
          + "LEFT JOIN LATERAL (\n"
          + "    SELECT le.run_uuid, JSON_AGG(event->'run'->'facets') AS facets\n"
          + "    FROM lineage_events le\n"
          + "    WHERE le.run_uuid=r.uuid\n"
          + "    GROUP BY le.run_uuid\n"
          + ") AS f ON r.uuid=f.run_uuid\n"
          + "LEFT JOIN LATERAL (\n"
          + "    SELECT im.run_uuid,\n"
          + "           JSON_AGG(json_build_object('namespace', dv.namespace_name,\n"
          + "                                      'name', dv.dataset_name,\n"
          + "                                      'version', dv.version)) AS input_versions\n"
          + "    FROM runs_input_mapping im\n"
          + "    INNER JOIN dataset_versions dv on im.dataset_version_uuid = dv.uuid\n"
          + "    WHERE im.run_uuid=r.uuid\n"
          + "    GROUP BY im.run_uuid\n"
          + ") ri ON ri.run_uuid=r.uuid\n"
          + "LEFT JOIN LATERAL (\n"
          + "    SELECT run_uuid, JSON_AGG(json_build_object('namespace', namespace_name,\n"
          + "                                                'name', dataset_name,\n"
          + "                                                'version', version)) AS output_versions\n"
          + "    FROM dataset_versions\n"
          + "    WHERE run_uuid=r.uuid\n"
          + "    GROUP BY run_uuid\n"
          + ") ro ON ro.run_uuid=r.uuid")
  List<Run> getCurrentRunsWithFacets(@BindList Collection<UUID> jobUuid);

  @SqlQuery(
      """
      SELECT DISTINCT on(r.job_name, r.namespace_name) r.*, jv.version as job_version
      FROM runs_view r
      INNER JOIN job_versions jv ON jv.uuid=r.job_version_uuid
      INNER JOIN jobs_view j ON j.uuid=jv.job_uuid
      WHERE j.uuid in (<jobUuid>) OR j.symlink_target_uuid IN (<jobUuid>)
      ORDER BY r.job_name, r.namespace_name, created_at DESC""")
  List<Run> getCurrentRuns(@BindList Collection<UUID> jobUuid);
}
