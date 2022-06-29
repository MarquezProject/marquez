/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.graphql;

import java.util.List;
import java.util.UUID;
import marquez.db.JobVersionDao.IoType;
import marquez.graphql.mapper.LineageResultMapper;
import marquez.graphql.mapper.LineageResultMapper.JobResult;
import marquez.graphql.mapper.ObjectMapMapper;
import marquez.graphql.mapper.RowMap;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(ObjectMapMapper.class)
@RegisterRowMapper(LineageResultMapper.class)
public interface GraphqlDaos extends SqlObject {
  /*
   * Note: Use must use a non-map type for returning single entries because a type of Map is already
   * registered to jdbi.
   */
  @SqlQuery("SELECT * FROM datasets WHERE uuid = :uuid ORDER BY updated_at")
  RowMap<String, Object> getDataset(UUID uuid);

  @SqlQuery("SELECT * FROM datasets")
  List<RowMap<String, Object>> getDatasets();

  @SqlQuery(
      "SELECT * FROM datasets where namespace_name = :namespaceName and datasets.name = :name")
  RowMap<String, Object> getDatasetByNamespaceAndName(String namespaceName, String name);

  @SqlQuery("SELECT * FROM jobs_view where namespace_name = :namespaceName and name = :name")
  RowMap<String, Object> getJobByNamespaceAndName(String namespaceName, String name);

  @SqlQuery("SELECT * FROM datasets where namespace_name = :namespaceName and name = :name")
  RowMap<String, Object> getDatasetsByNamespaceAndName(String namespaceName, String name);

  @SqlQuery("SELECT * FROM jobs_view")
  List<RowMap<String, Object>> getJobs();

  @SqlQuery("SELECT * FROM sources where uuid = :uuid")
  RowMap<String, Object> getSource(UUID uuid);

  @SqlQuery("SELECT * FROM namespaces where uuid = :uuid")
  RowMap<String, Object> getNamespace(UUID uuid);

  @SqlQuery("SELECT * FROM dataset_fields where dataset_uuid = :datasetUuid")
  List<RowMap<String, Object>> getDatasetField(UUID datasetUuid);

  @SqlQuery(
      "SELECT f.* FROM dataset_fields f inner join dataset_fields_tag_mapping m on m.dataset_field_uuid = f.uuid where m.tag_uuid = :tagUuid")
  List<RowMap<String, Object>> getDatasetFieldsByTagUuid(UUID tagUuid);

  @SqlQuery(
      "SELECT d.* FROM datasets d inner join datasets_tag_mapping m on m.dataset_uuid = d.uuid where tag_uuid = :uuid")
  List<RowMap<String, Object>> getDatasetsByTagUuid(UUID tagUuid);

  @SqlQuery("SELECT d.* from datasets d where source_uuid = :sourceUuid")
  List<RowMap<String, Object>> getDatasetsBySource(UUID sourceUuid);

  @SqlQuery("SELECT * from runs_view where uuid = :uuid")
  RowMap<String, Object> getRun(UUID uuid);

  @SqlQuery("SELECT * from runs_view where run_args_uuid = :runArgsUuid")
  List<RowMap<String, Object>> getRunsByRunArgs(UUID runArgsUuid);

  @SqlQuery("SELECT * FROM dataset_versions where uuid = :uuid")
  RowMap<String, Object> getCurrentDatasetVersion(UUID uuid);

  @SqlQuery(
      "SELECT dv.* from dataset_versions dv inner join runs_input_mapping m on m.dataset_version_uuid = dv.uuid where m.run_uuid = :runUuid")
  List<RowMap<String, Object>> getDatasetVersionInputsByRun(UUID runUuid);

  @SqlQuery(
      "SELECT r.* from runs_view r inner join runs_input_mapping m on m.run_uuid = r.uuid where m.dataset_version_uuid = :datasetVersionUuid")
  List<RowMap<String, Object>> getRunsByDatasetVersion(UUID datasetVersionUuid);

  @SqlQuery(
      "SELECT distinct jv.* from runs_view r inner join runs_input_mapping m on m.run_uuid = r.uuid inner join job_versions jv on jv.uuid = r.job_version_uuid where m.dataset_version_uuid = :datasetVersionUuid")
  List<RowMap<String, Object>> getDistinctJobVersionsByDatasetVersion(UUID datasetVersionUuid);

  @SqlQuery(
      "SELECT distinct jv.* from dataset_versions dv inner join runs_view r on r.uuid = dv.run_uuid inner join job_versions jv on jv.uuid = r.job_version_uuid where dv.uuid = :datasetVersionUuid")
  List<RowMap<String, Object>> getDistinctJobVersionsByDatasetVersionOutput(
      UUID datasetVersionUuid);

  @SqlQuery("SELECT dv.* from dataset_versions dv where dv.run_uuid = :runUuid")
  List<RowMap<String, Object>> getDatasetVersionByRun(UUID runUuid);

  @SqlQuery("SELECT * from run_args where uuid = :uuid")
  RowMap<String, Object> getRunArgs(UUID uuid);

  @SqlQuery(
      "SELECT n.* from namespaces n inner join on namespace_ownerships no on no.namespace_uuid = n.uuid where owner_uuid = :ownerUuid")
  List<RowMap<String, Object>> getNamespacesByOwner(UUID ownerUuid);

  @SqlQuery(
      "SELECT * from owners o inner join namespace_ownerships no on o.uuid = no.owner_uuid where namespace_uuid = :namespaceUuid")
  List<RowMap<String, Object>> getOwnersByNamespace(UUID namespaceUuid);

  @SqlQuery("SELECT * from owners where name = :ownerName")
  RowMap<String, Object> getCurrentOwnerByNamespace(String ownerName);

  @SqlQuery("SELECT * from job_contexts where uuid = :uuid")
  RowMap<String, Object> getJobContext(UUID uuid);

  @SqlQuery("SELECT * from datasets where namespace_uuid = :namespaceUuid")
  List<RowMap<String, Object>> getDatasetsByNamespace(UUID namespaceUuid);

  @SqlQuery(
      "SELECT d.* from datasets d inner join job_versions_io_mapping m on m.dataset_uuid = d.uuid where m.job_version_uuid = :jobVersionUuid and io_type = :ioType")
  List<RowMap<String, Object>> getIOMappingByJobVersion(UUID jobVersionUuid, IoType ioType);

  @SqlQuery(
      "SELECT jv.uuid, jv.created_at, jv.updated_at, jv.job_uuid, jv.version, jv.location, "
          + " jv.latest_run_uuid, jv.job_context_uuid, j.namespace_uuid, j.namespace_name, "
          + " j.name AS job_name "
          + " FROM job_versions_io_mapping m "
          + " inner join job_versions jv "
          + " on m.dataset_uuid = jv.uuid"
          + " inner join jobs_view j ON j.uuid=jv.job_uuid "
          + " where m.dataset_uuid = :datasetUuid AND m.io_type = :ioType")
  List<RowMap<String, Object>> getJobVersionsByIoMapping(UUID datasetUuid, IoType ioType);

  @SqlQuery(
      "SELECT jv.uuid, jv.created_at, jv.updated_at, jv.job_uuid, jv.version, jv.location, "
          + " jv.latest_run_uuid, jv.job_context_uuid, j.namespace_uuid, j.namespace_name, "
          + " j.name AS job_name "
          + " from job_versions jv "
          + " inner join jobs_view j ON j.uuid=jv.job_uuid "
          + " where job_uuid = :jobUuid")
  List<RowMap<String, Object>> getJobVersionByJob(UUID jobUuid);

  @SqlQuery(
      "SELECT jv.uuid, jv.created_at, jv.updated_at, jv.job_uuid, jv.version, jv.location, "
          + " jv.latest_run_uuid, jv.job_context_uuid, j.namespace_uuid, j.namespace_name, "
          + " j.name AS job_name "
          + " from job_versions jv "
          + " inner join jobs_view j ON j.uuid=jv.job_uuid "
          + " where jv.uuid = :uuid")
  RowMap<String, Object> getJobVersion(UUID uuid);

  @SqlQuery("SELECT * from dataset_fields where dataset_uuid = :datasetVersionUuid")
  List<RowMap<String, Object>> getFields(UUID datasetVersionUuid);

  @SqlQuery(
      "SELECT dv.* from dataset_versions dv inner join dataset_versions_field_mapping m on dv.uuid = m.dataset_version_uuid where dataset_field_uuid = :datasetFieldUuid")
  List<RowMap<String, Object>> getVersionsByDatasetField(UUID datasetFieldUuid);

  @SqlQuery("SELECT * FROM dataset_versions where dataset_uuid = :datasetUuid")
  List<RowMap<String, Object>> getDatasetVersionsByDataset(UUID datasetUuid);

  @SqlQuery("SELECT * FROM namespaces where name = :name")
  RowMap<String, Object> getNamespaceByName(String name);

  @SqlQuery("SELECT * from jobs_view where namespace_uuid = :namespaceUuid")
  List<RowMap<String, Object>> getJobsByNamespace(UUID namespaceUuid);

  @SqlQuery("SELECT * from jobs_view where uuid = :uuid")
  RowMap<String, Object> getJob(UUID uuid);

  @SqlQuery("SELECT * from run_states where run_uuid = :runUuid order by transitioned_at desc")
  List<RowMap<String, Object>> getRunStateByRun(UUID runUuid);

  @SqlQuery("SELECT * from run_states where uuid = :uuid")
  RowMap<String, Object> getRunStateByUuid(UUID uuid);

  @SqlQuery(
      "SELECT t.* FROM datasets_tag_mapping m "
          + " inner join tags t "
          + " on m.tag_uuid = t.uuid"
          + " where dataset_uuid = :datasetUuid")
  List<RowMap<String, Object>> getTagsByDatasetTag(UUID datasetUuid);

  @SqlQuery(
      "SELECT t.* from tags t inner join dataset_fields_tag_mapping m on t.uuid = m.tag_uuid where dataaset_field_uuid = :datasetFieldUuid")
  List<RowMap<String, Object>> getTagsByDatasetField(UUID datasetFieldUuid);

  @SqlQuery(
      """
      select distinct on (lineage.job_name,
                 lineage.namespace_name)
                 lineage.job_name as name,
                 lineage.namespace_name as namespace,
                 d_in.agg as "inEdges",
                 d_out.agg as "outEdges"
          from (
              WITH RECURSIVE search_graph(job_name, namespace_name, depth, path, cycle) AS (
                  select j.name, j.namespace_name, 1, ARRAY[j.name], false
                  from jobs_view j
                  where name = :jobName and j.namespace_name = :namespaceName
                  UNION ALL
                  select l.job_name, l.namespace_name, depth+1, (path || l.job_name), l.job_name = ANY(path)
                  from search_graph sg,
                  (
                      select j.name AS job_name, j.namespace_name, j.name as jx
                      from jobs_view j
                      inner join job_versions_io_mapping io_in on io_in.job_version_uuid = j.current_version_uuid and io_in.io_type = 'INPUT'
                      inner join job_versions_io_mapping io_out on io_out.dataset_uuid = io_in.dataset_uuid and io_out.io_type = 'OUTPUT'
                      inner join job_versions jv on jv.uuid = io_out.job_version_uuid
                      UNION ALL
                      select j.name AS job_name, jv.namespace_name, j.name as jx
                      from jobs_view j
                      inner join job_versions_io_mapping io_out on io_out.job_version_uuid = j.current_version_uuid and io_out.io_type = 'OUTPUT'
                      inner join job_versions_io_mapping io_in on io_in.dataset_uuid = io_out.dataset_uuid and io_in.io_type = 'INPUT'
                      inner join job_versions jv on jv.uuid = io_in.job_version_uuid
                  ) l where l.jx = sg.job_name and NOT cycle
              )
              SELECT * FROM search_graph where NOT cycle and depth <= :depth
          ) lineage
          -- Construct the dataset edges:
          inner join jobs_view j on lineage.job_name = j.name and lineage.namespace_name = j.namespace_name
          -- input datasets
          left outer join (
              select io_out.job_version_uuid, jsonb_agg((SELECT x FROM (SELECT ds_in.name, ds_in.namespace_name as namespace,  o.out_agg as "inEdges", i.in_agg as "outEdges") AS x)) as agg
              from job_versions_io_mapping io_out
              inner join datasets ds_in on ds_in.uuid = io_out.dataset_uuid
          -- output jobs for each input dataset
              left outer join (
                   select io_of_in.dataset_uuid, jsonb_agg((select x from (select j_of_in.name, j_of_in.namespace_name as namespace) as x)) as in_agg
                   from jobs_view j_of_in
                   left outer join job_versions_io_mapping io_of_in on io_of_in.job_version_uuid = j_of_in.current_version_uuid
                    and io_of_in.io_type = 'INPUT'
                   group by io_of_in.dataset_uuid
              ) i on i.dataset_uuid = io_out.dataset_uuid
          -- input jobs for each input dataset
              left outer join (
                  select io_of_out.dataset_uuid, jsonb_agg((select x from (select j_of_out.name, j_of_out.namespace_name as namespace) as x)) as out_agg
                  from jobs_view j_of_out
                  left outer join job_versions_io_mapping io_of_out
                    on io_of_out.job_version_uuid = j_of_out.current_version_uuid
                    and io_of_out.io_type = 'OUTPUT'
                  group by io_of_out.dataset_uuid
              ) o on o.dataset_uuid = io_out.dataset_uuid
              WHERE io_out.io_type = 'OUTPUT'
              group by io_out.job_version_uuid

          ) d_in on d_in.job_version_uuid = j.current_version_uuid
          --output datasets
          left outer join(
              select io_out.job_version_uuid, jsonb_agg((SELECT x FROM (SELECT ds_in.name, ds_in.namespace_name as namespace, o.out_agg as "inEdges", i.in_agg as "outEdges") AS x)) as agg
              from job_versions_io_mapping io_out
              inner join datasets ds_in on ds_in.uuid = io_out.dataset_uuid
              -- output jobs for each output dataset
              left outer join (
                   select io_of_in.dataset_uuid, jsonb_agg((select x from (select j_of_in.name, j_of_in.namespace_name as namespace) as x)) as in_agg
                   from jobs_view j_of_in
                   left outer join job_versions_io_mapping io_of_in on io_of_in.job_version_uuid = j_of_in.current_version_uuid
                    and io_of_in.io_type = 'INPUT'
                   group by io_of_in.dataset_uuid
              ) i on i.dataset_uuid = io_out.dataset_uuid
              -- input jobs for each output dataset
              left outer join (
                  select io_of_out.dataset_uuid, jsonb_agg((select x from (select j_of_out.name, j_of_out.namespace_name as namespace) as x)) as out_agg
                  from jobs_view j_of_out
                  left outer join job_versions_io_mapping io_of_out
                    on io_of_out.job_version_uuid = j_of_out.current_version_uuid
                    and io_of_out.io_type = 'OUTPUT'
                  group by io_of_out.dataset_uuid
              ) o on o.dataset_uuid = io_out.dataset_uuid
              WHERE io_out.io_type = 'INPUT'
              group by io_out.job_version_uuid
          ) d_out on d_out.job_version_uuid = j.current_version_uuid
  """)
  List<JobResult> getLineage(String jobName, String namespaceName, Integer depth);
}
