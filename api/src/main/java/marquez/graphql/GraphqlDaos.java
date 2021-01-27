package marquez.graphql;

import java.util.List;
import java.util.UUID;
import marquez.db.JobVersionDao.IoType;
import marquez.graphql.mapper.ObjectMapMapper;
import marquez.graphql.mapper.RowMap;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(ObjectMapMapper.class)
public interface GraphqlDaos extends SqlObject {
  /*
   * Note: Use must use a non-map type for returning single entries because a type of Map is already
   * registered to jdbi.
   */
  @SqlQuery("SELECT * FROM datasets WHERE uuid = :uuid ORDER BY updated_at")
  RowMap<String, Object> getDataset(UUID uuid);

  @SqlQuery("SELECT * FROM datasets")
  List<RowMap<String, Object>> getDatasets();

  @SqlQuery("SELECT * FROM jobs")
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

  @SqlQuery("SELECT * from runs where uuid = :uuid")
  RowMap<String, Object> getRun(UUID uuid);

  @SqlQuery("SELECT * from runs where run_args_uuid = :runArgsUuid")
  List<RowMap<String, Object>> getRunsByRunArgs(UUID runArgsUuid);

  @SqlQuery("SELECT * FROM dataset_versions where uuid = :uuid")
  List<RowMap<String, Object>> getCurrentDatasetVersion(UUID uuid);

  @SqlQuery(
      "SELECT dv.* from dataset_versions dv inner join runs_input_mapping m on m.dataset_version_uuid = dv.uuid where m.run_uuid = :runUuid")
  List<RowMap<String, Object>> getInputsByRun(UUID runUuid);

  @SqlQuery("SELECT dv.* from dataset_versions dv where dv.run_uuid = :runUuid")
  List<RowMap<String, Object>> getOutputsByRun(UUID runUuid);

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
      "SELECT jv.* "
          + " FROM job_versions_io_mapping m "
          + " inner join job_versions jv "
          + " on m.dataset_uuid = jv.uuid"
          + " where m.dataset_uuid = :datasetUuid AND m.io_type = :ioType")
  List<RowMap<String, Object>> getJobVersionsByIoMapping(UUID datasetUuid, IoType ioType);

  @SqlQuery("SELECT * from job_versions where job_context_uuid = :jobContextUuid")
  List<RowMap<String, Object>> getJobVersionByJobContext(UUID jobContextUuid);

  @SqlQuery("SELECT * from job_versions where job_uuid = :jobUuid")
  List<RowMap<String, Object>> getJobVersionByJob(UUID jobUuid);

  @SqlQuery("SELECT * from job_versions where uuid = :uuid")
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

  @SqlQuery("SELECT * from jobs where namespace_uuid = :namespaceUuid")
  List<RowMap<String, Object>> getJobsByNamespace(UUID namespaceUuid);

  @SqlQuery("SELECT * from jobs where uuid = :uuid")
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
      "select distinct name, max(rank) as rank from  "
          + "( "
          // Exact match
          + "( "
          + "SELECT name, 1 AS rank  "
          + "FROM jobs "
          + "WHERE name = :term "
          + ")"
          + "UNION "
          // Ranked match
          + "( "
          + "SELECT name, ts_rank_cd(to_tsvector('simple', regexp_replace(name, '\\.|\\\\|:|/|-|_', ' ', 'g')), query.q) AS rank  "
          + "FROM jobs, (select :query::tsquery q ) query  "
          + "WHERE query.q @@ regexp_replace(name, '\\.|\\\\|:|/|-|_', ' ', 'g')::tsvector  "
          + "ORDER BY rank DESC  "
          + "LIMIT 50 "
          + ") "
          + "UNION "
          // String contains
          + "( "
          + "SELECT name, 0 AS rank  "
          + "FROM jobs "
          + "WHERE name ilike '%' || :term || '%' "
          + "ORDER BY rank DESC "
          + "LIMIT 50 "
          + ")"
          + ") q group by name order by rank DESC LIMIT 50")
  List<RowMap<String, Object>> searchJobs(String query, String term);

  @SqlQuery(
      "select distinct name, max(rank) as rank from  "
          + "( "
          // Exact match
          + "( "
          + "SELECT name, 1 AS rank  "
          + "FROM datasets "
          + "WHERE name = :term "
          + ")"
          + "UNION "
          // Ranked match
          + "( "
          + "SELECT name, ts_rank_cd(to_tsvector('simple', regexp_replace(name, '\\.|\\\\|:|/|-|_', ' ', 'g')), query.q) AS rank  "
          + "FROM datasets, (select :query::tsquery q ) query  "
          + "WHERE query.q @@ regexp_replace(name, '\\.|\\\\|:|/|-|_', ' ', 'g')::tsvector  "
          + "ORDER BY rank DESC  "
          + "LIMIT 50 "
          + ") "
          + "UNION "
          // String contains
          + "( "
          + "SELECT name, 0 AS rank  "
          + "FROM datasets "
          + "WHERE name ilike '%' || :term || '%' "
          + "ORDER BY rank DESC "
          + "LIMIT 50 "
          + ")"
          + ") q group by name order by rank DESC LIMIT 50")
  List<RowMap<String, Object>> searchDatasets(String query, String term);
}
