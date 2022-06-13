/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.Field;
import marquez.common.models.FieldName;
import marquez.common.models.RunId;
import marquez.common.models.TagName;
import marquez.common.models.Version;
import marquez.db.DatasetFieldDao.DatasetFieldMapping;
import marquez.db.DatasetFieldDao.DatasetFieldTag;
import marquez.db.mappers.DatasetVersionMapper;
import marquez.db.mappers.DatasetVersionRowMapper;
import marquez.db.mappers.ExtendedDatasetVersionRowMapper;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.TagRow;
import marquez.service.DatasetService;
import marquez.service.models.DatasetMeta;
import marquez.service.models.DatasetVersion;
import marquez.service.models.LineageEvent.SchemaField;
import marquez.service.models.Run;
import marquez.service.models.StreamMeta;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;

@RegisterRowMapper(DatasetVersionRowMapper.class)
@RegisterRowMapper(ExtendedDatasetVersionRowMapper.class)
@RegisterRowMapper(DatasetVersionMapper.class)
public interface DatasetVersionDao extends BaseDao {

  @Transaction
  default DatasetVersionRow upsertDatasetVersion(
      UUID datasetUuid,
      Instant now,
      String namespaceName,
      String datasetName,
      String lifecycleState,
      DatasetMeta datasetMeta) {
    TagDao tagDao = createTagDao();
    DatasetFieldDao datasetFieldDao = createDatasetFieldDao();

    Version version = Utils.newDatasetVersionFor(namespaceName, datasetName, datasetMeta);
    UUID newDatasetVersionUuid = UUID.randomUUID();
    DatasetVersionRow datasetVersionRow =
        upsert(
            newDatasetVersionUuid,
            now,
            datasetUuid,
            version.getValue(),
            datasetMeta.getRunId().map(RunId::getValue).orElse(null),
            toPgObjectFields(datasetMeta.getFields()),
            namespaceName,
            datasetName,
            lifecycleState);
    updateDatasetVersionMetric(
        namespaceName,
        datasetMeta.getType().toString(),
        datasetName,
        newDatasetVersionUuid,
        datasetVersionRow.getUuid());

    if (datasetMeta instanceof StreamMeta) {
      createStreamVersionDao()
          .insert(
              datasetVersionRow.getUuid(),
              ((StreamMeta) datasetMeta).getSchemaLocation().toString());
    }

    List<DatasetFieldMapping> datasetFieldMappings = new ArrayList<>();
    List<DatasetFieldTag> datasetFieldTag = new ArrayList<>();

    for (Field field : datasetMeta.getFields()) {
      DatasetFieldRow datasetFieldRow =
          datasetFieldDao.upsert(
              UUID.randomUUID(),
              now,
              field.getName().getValue(),
              field.getType(),
              field.getDescription().orElse(null),
              datasetUuid);
      for (TagName tagName : field.getTags()) {
        TagRow tag = tagDao.upsert(UUID.randomUUID(), now, tagName.getValue());
        datasetFieldTag.add(new DatasetFieldTag(datasetFieldRow.getUuid(), tag.getUuid(), now));
      }
      datasetFieldMappings.add(
          new DatasetFieldMapping(datasetVersionRow.getUuid(), datasetFieldRow.getUuid()));
    }

    datasetFieldDao.updateFieldMapping(datasetFieldMappings);
    datasetFieldDao.updateTags(datasetFieldTag);

    createDatasetDao().updateVersion(datasetUuid, now, datasetVersionRow.getUuid());
    return datasetVersionRow;
  }

  default PGobject toPgObjectFields(List<Field> fields) {
    if (fields == null) {
      return null;
    }
    try {
      PGobject jsonObject = new PGobject();
      jsonObject.setType("json");
      jsonObject.setValue(Utils.getMapper().writeValueAsString(fields));
      return jsonObject;
    } catch (Exception e) {
      return null;
    }
  }

  default PGobject toPgObjectSchemaFields(List<SchemaField> fields) {
    return toPgObjectFields(toFields(fields));
  }

  default List<Field> toFields(List<SchemaField> fields) {
    if (fields == null) {
      return null;
    }
    OpenLineageDao openLineageDao = createOpenLineageDao();
    return fields.stream()
        .map(
            f ->
                new Field(
                    FieldName.of(f.getName()), f.getType(), ImmutableSet.of(), f.getDescription()))
        .collect(Collectors.toList());
  }

  default void updateDatasetVersionMetric(
      String namespaceName,
      String type,
      String datasetName,
      UUID newDatasetVersionUuid,
      UUID datasetVersionUuid) {
    if (newDatasetVersionUuid != datasetVersionUuid) {
      DatasetService.versions.labels(namespaceName, type, datasetName).inc();
    }
  }

  String SELECT = "SELECT dv.* " + "FROM dataset_versions dv ";

  @SqlQuery(
      "WITH selected_dataset_versions AS (\n"
          + "    SELECT dv.*\n"
          + "    FROM dataset_versions dv\n"
          + "    WHERE dv.version = :version\n"
          + "), selected_dataset_version_runs AS (\n"
          + "    SELECT uuid, dataset_uuid, namespace_name, dataset_name, version, created_at, run_uuid\n"
          + "    FROM selected_dataset_versions\n"
          + "    UNION\n"
          + "    SELECT DISTINCT dv.uuid, dv.dataset_uuid, dv.namespace_name, dv.dataset_name, dv.version, dv.created_at, rim.run_uuid\n"
          + "    FROM selected_dataset_versions dv\n"
          + "    LEFT JOIN runs_input_mapping rim\n"
          + "         ON rim.dataset_version_uuid = dv.uuid\n"
          + "), selected_dataset_version_events AS (\n"
          + "    SELECT dv.uuid, dv.dataset_name, dv.namespace_name, dv.run_uuid, le.event_time, le.event\n"
          + "    FROM selected_dataset_version_runs dv\n"
          + "    LEFT JOIN lineage_events le ON le.run_uuid = dv.run_uuid\n"
          + ")\n"
          + "SELECT d.type, d.name, d.physical_name, d.namespace_name, d.source_name, d.description, dv.lifecycle_state, \n"
          + "    dv.created_at, dv.version, dv.fields, dv.run_uuid AS createdByRunUuid, sv.schema_location,\n"
          + "    t.tags, f.facets\n"
          + "FROM selected_dataset_versions dv\n"
          + "LEFT JOIN datasets d ON d.uuid = dv.dataset_uuid\n"
          + "LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid\n"
          + "LEFT JOIN (\n"
          + "    SELECT ARRAY_AGG(t.name) AS tags, m.dataset_uuid\n"
          + "    FROM tags AS t\n"
          + "             INNER JOIN datasets_tag_mapping AS m ON m.tag_uuid = t.uuid\n"
          + "    GROUP BY m.dataset_uuid\n"
          + ") t ON t.dataset_uuid = dv.dataset_uuid\n"
          + "LEFT JOIN (\n"
          + "    SELECT dve.uuid AS dataset_uuid, JSONB_AGG(ds->'facets' ORDER BY event_time ASC) AS facets\n"
          + "    FROM selected_dataset_version_events dve,\n"
          + "         jsonb_array_elements(coalesce(dve.event -> 'inputs', '[]'::jsonb) || coalesce(dve.event -> 'outputs', '[]'::jsonb)) AS ds\n"
          + "    WHERE dve.run_uuid = dve.run_uuid\n"
          + "      AND ds -> 'facets' IS NOT NULL\n"
          + "      AND ds ->> 'name' = dve.dataset_name\n"
          + "      AND ds ->> 'namespace' = dve.namespace_name\n"
          + "    GROUP BY dve.uuid\n"
          + ") f ON f.dataset_uuid = dv.uuid")
  Optional<DatasetVersion> findBy(UUID version);

  @SqlQuery(
      "WITH selected_dataset_versions AS (\n"
          + "    SELECT dv.*\n"
          + "    FROM dataset_versions dv\n"
          + "    WHERE dv.uuid = :uuid\n"
          + "), selected_dataset_version_runs AS (\n"
          + "    SELECT uuid, dataset_uuid, namespace_name, dataset_name, version, created_at, run_uuid\n"
          + "    FROM selected_dataset_versions\n"
          + "    UNION\n"
          + "    SELECT DISTINCT dv.uuid, dv.dataset_uuid, dv.namespace_name, dv.dataset_name, dv.version, dv.created_at, rim.run_uuid\n"
          + "    FROM selected_dataset_versions dv\n"
          + "    LEFT JOIN runs_input_mapping rim\n"
          + "         ON rim.dataset_version_uuid = dv.uuid\n"
          + "), selected_dataset_version_events AS (\n"
          + "    SELECT dv.uuid, dv.dataset_name, dv.namespace_name, dv.run_uuid, le.event_time, le.event\n"
          + "    FROM selected_dataset_version_runs dv\n"
          + "    LEFT JOIN lineage_events le ON le.run_uuid = dv.run_uuid\n"
          + ")\n"
          + "SELECT d.type, d.name, d.physical_name, d.namespace_name, d.source_name, d.description, dv.lifecycle_state, \n"
          + "    dv.created_at, dv.version, dv.fields, dv.run_uuid AS createdByRunUuid, sv.schema_location,\n"
          + "    t.tags, f.facets\n"
          + "FROM selected_dataset_versions dv\n"
          + "LEFT JOIN datasets d ON d.uuid = dv.dataset_uuid\n"
          + "LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid\n"
          + "LEFT JOIN (\n"
          + "    SELECT ARRAY_AGG(t.name) AS tags, m.dataset_uuid\n"
          + "    FROM tags AS t\n"
          + "             INNER JOIN datasets_tag_mapping AS m ON m.tag_uuid = t.uuid\n"
          + "    GROUP BY m.dataset_uuid\n"
          + ") t ON t.dataset_uuid = dv.dataset_uuid\n"
          + "LEFT JOIN (\n"
          + "    SELECT dve.uuid AS dataset_uuid, JSONB_AGG(ds->'facets' ORDER BY event_time ASC) AS facets\n"
          + "    FROM selected_dataset_version_events dve,\n"
          + "         jsonb_array_elements(coalesce(dve.event -> 'inputs', '[]'::jsonb) || coalesce(dve.event -> 'outputs', '[]'::jsonb)) AS ds\n"
          + "    WHERE dve.run_uuid = dve.run_uuid\n"
          + "      AND ds -> 'facets' IS NOT NULL\n"
          + "      AND ds ->> 'name' = dve.dataset_name\n"
          + "      AND ds ->> 'namespace' = dve.namespace_name\n"
          + "    GROUP BY dve.uuid\n"
          + ") f ON f.dataset_uuid = dv.uuid")
  Optional<DatasetVersion> findByUuid(UUID uuid);

  default Optional<DatasetVersion> findByWithRun(UUID version) {
    Optional<DatasetVersion> v = findBy(version);

    v.ifPresent(
        ver -> {
          if (ver.getCreatedByRunUuid() != null) {
            Optional<Run> run = createRunDao().findRunByUuid(ver.getCreatedByRunUuid());
            run.ifPresent(ver::setCreatedByRun);
          }
        });
    return v;
  }

  @SqlQuery(
      SELECT
          + " INNER JOIN runs_input_mapping m ON m.dataset_version_uuid = dv.uuid WHERE m.run_uuid = :runUuid")
  List<ExtendedDatasetVersionRow> findInputDatasetVersionsFor(UUID runUuid);

  /**
   * returns all Dataset Versions created by this run id
   *
   * @param runId - the run ID
   */
  @SqlQuery(SELECT + " WHERE run_uuid = :runId")
  List<ExtendedDatasetVersionRow> findOutputDatasetVersionsFor(@NonNull UUID runId);

  @SqlQuery(
      "WITH selected_dataset_versions AS (\n"
          + "    SELECT dv.*\n"
          + "    FROM dataset_versions dv\n"
          + "    WHERE dv.namespace_name = :namespaceName\n"
          + "      AND dv.dataset_name = :datasetName\n"
          + "    ORDER BY dv.created_at DESC\n"
          + "    LIMIT :limit OFFSET :offset\n"
          + "), selected_dataset_version_runs AS (\n"
          + "    SELECT uuid, dataset_uuid, namespace_name, dataset_name, version, created_at, run_uuid\n"
          + "    FROM selected_dataset_versions\n"
          + "    UNION\n"
          + "    SELECT DISTINCT dv.uuid, dv.dataset_uuid, dv.namespace_name, dv.dataset_name, dv.version, dv.created_at, rim.run_uuid\n"
          + "    FROM selected_dataset_versions dv\n"
          + "    LEFT JOIN runs_input_mapping rim\n"
          + "         ON rim.dataset_version_uuid = dv.uuid\n"
          + "), selected_dataset_version_events AS (\n"
          + "    SELECT dv.uuid, dv.dataset_name, dv.namespace_name, dv.run_uuid, le.event_time, le.event\n"
          + "    FROM selected_dataset_version_runs dv\n"
          + "    LEFT JOIN lineage_events le ON le.run_uuid = dv.run_uuid\n"
          + ")\n"
          + "SELECT d.type, d.name, d.physical_name, d.namespace_name, d.source_name, d.description, dv.lifecycle_state,\n"
          + "    dv.created_at, dv.version, dv.fields, dv.run_uuid AS createdByRunUuid, sv.schema_location,\n"
          + "    t.tags, f.facets\n"
          + "FROM selected_dataset_versions dv\n"
          + "LEFT JOIN datasets d ON d.uuid = dv.dataset_uuid\n"
          + "LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid\n"
          + "LEFT JOIN (\n"
          + "    SELECT ARRAY_AGG(t.name) AS tags, m.dataset_uuid\n"
          + "    FROM tags AS t\n"
          + "             INNER JOIN datasets_tag_mapping AS m ON m.tag_uuid = t.uuid\n"
          + "    GROUP BY m.dataset_uuid\n"
          + ") t ON t.dataset_uuid = dv.dataset_uuid\n"
          + "LEFT JOIN (\n"
          + "    SELECT dve.uuid AS dataset_uuid, JSONB_AGG(ds->'facets' ORDER BY event_time ASC) AS facets\n"
          + "    FROM selected_dataset_version_events dve,\n"
          + "         jsonb_array_elements(coalesce(dve.event -> 'inputs', '[]'::jsonb) || coalesce(dve.event -> 'outputs', '[]'::jsonb)) AS ds\n"
          + "    WHERE dve.run_uuid = dve.run_uuid\n"
          + "      AND ds -> 'facets' IS NOT NULL\n"
          + "      AND ds ->> 'name' = dve.dataset_name\n"
          + "      AND ds ->> 'namespace' = dve.namespace_name\n"
          + "    GROUP BY dve.uuid\n"
          + ") f ON f.dataset_uuid = dv.uuid\n"
          + "ORDER BY dv.created_at DESC")
  List<DatasetVersion> findAll(String namespaceName, String datasetName, int limit, int offset);

  default List<DatasetVersion> findAllWithRun(
      String namespaceName, String datasetName, int limit, int offset) {
    List<DatasetVersion> v = findAll(namespaceName, datasetName, limit, offset);
    return v.stream()
        .peek(
            ver -> {
              if (ver.getCreatedByRunUuid() != null) {
                Optional<Run> run = createRunDao().findRunByUuid(ver.getCreatedByRunUuid());
                run.ifPresent(ver::setCreatedByRun);
              }
            })
        .collect(Collectors.toList());
  }

  @SqlQuery(SELECT + "WHERE dv.uuid = :uuid")
  Optional<DatasetVersionRow> findRowByUuid(UUID uuid);

  @SqlQuery(
      "INSERT INTO dataset_versions "
          + "(uuid, created_at, dataset_uuid, version, run_uuid, fields, namespace_name, dataset_name, lifecycle_state) "
          + "VALUES "
          + "(:uuid, :now, :datasetUuid, :version, :runUuid, :fields, :namespaceName, :datasetName, :lifecycleState) "
          + "ON CONFLICT(version) "
          + "DO UPDATE SET "
          + "run_uuid = EXCLUDED.run_uuid "
          + "RETURNING *")
  DatasetVersionRow upsert(
      UUID uuid,
      Instant now,
      UUID datasetUuid,
      UUID version,
      UUID runUuid,
      PGobject fields,
      String namespaceName,
      String datasetName,
      String lifecycleState);

  @SqlUpdate("UPDATE dataset_versions SET fields = :fields WHERE uuid = :uuid")
  void updateFields(UUID uuid, PGobject fields);
}
