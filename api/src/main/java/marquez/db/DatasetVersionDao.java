/*
 * Copyright 2018-2023 contributors to the Marquez project
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
import marquez.db.models.DatasetRow;
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
      DatasetRow datasetRow,
      Instant now,
      String namespaceName,
      String datasetName,
      String lifecycleState,
      DatasetMeta datasetMeta) {
    TagDao tagDao = createTagDao();
    DatasetFieldDao datasetFieldDao = createDatasetFieldDao();

    List<DatasetFieldRow> datasetFields = new ArrayList<>();
    List<DatasetFieldTag> datasetFieldTags = new ArrayList<>();
    for (Field field : datasetMeta.getFields()) {
      DatasetFieldRow datasetFieldRow =
          datasetFieldDao.upsert(
              UUID.randomUUID(),
              now,
              field.getName().getValue(),
              field.getType(),
              field.getDescription().orElse(null),
              datasetRow.getUuid());
      datasetFields.add(datasetFieldRow);
      for (TagName tagName : field.getTags()) {
        TagRow tag = tagDao.upsert(UUID.randomUUID(), now, tagName.getValue());
        datasetFieldTags.add(new DatasetFieldTag(datasetFieldRow.getUuid(), tag.getUuid(), now));
      }
    }
    datasetFieldDao.updateTags(datasetFieldTags);

    Version version = Utils.newDatasetVersionFor(namespaceName, datasetName, datasetMeta);
    UUID newDatasetVersionUuid = UUID.randomUUID();
    UUID datasetSchemaVersionUuid =
        createDatasetSchemaVersionDao()
            .upsertSchemaVersion(datasetRow, datasetFields, now)
            .getValue();
    DatasetVersionRow datasetVersionRow =
        upsert(
            newDatasetVersionUuid,
            now,
            datasetRow.getUuid(),
            version.getValue(),
            datasetSchemaVersionUuid,
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
    for (DatasetFieldRow datasetFieldRow : datasetFields) {
      datasetFieldMappings.add(
          new DatasetFieldMapping(datasetVersionRow.getUuid(), datasetFieldRow.getUuid()));
    }
    datasetFieldDao.updateFieldMapping(datasetFieldMappings);

    createDatasetDao().updateVersion(datasetRow.getUuid(), now, datasetVersionRow.getUuid());
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
      """
      WITH selected_dataset_versions AS (
          SELECT dv.*
          FROM dataset_versions dv
          WHERE dv.uuid = :version
      ), selected_dataset_version_facets AS (
          SELECT dv.uuid, dv.dataset_name, dv.namespace_name, df.run_uuid, df.lineage_event_time, df.facet
          FROM selected_dataset_versions dv
          LEFT JOIN dataset_facets_view df ON df.dataset_version_uuid = dv.uuid
      )
      SELECT d.type, d.name, d.physical_name, d.namespace_name, d.source_name, d.description, dv.lifecycle_state,\s
          dv.created_at, dv.uuid AS current_version_uuid, dv.version, dv.dataset_schema_version_uuid, dv.fields, dv.run_uuid AS createdByRunUuid,
          sv.schema_location, t.tags, f.facets
      FROM selected_dataset_versions dv
      LEFT JOIN datasets_view d ON d.uuid = dv.dataset_uuid
      LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid
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
  Optional<DatasetVersion> findBy(UUID version);

  @SqlQuery(
      """
      WITH selected_dataset_versions AS (
          SELECT dv.*
          FROM dataset_versions dv
          WHERE dv.uuid = :uuid
      ), selected_dataset_version_facets AS (
          SELECT dv.uuid, dv.dataset_name, dv.namespace_name, df.run_uuid, df.lineage_event_time, df.facet
          FROM selected_dataset_versions dv
          LEFT JOIN dataset_facets_view df ON df.dataset_version_uuid = dv.uuid  AND (df.type ILIKE 'dataset' OR df.type ILIKE 'unknown' OR df.type ILIKE 'input')
      )
      SELECT d.type, d.name, d.physical_name, d.namespace_name, d.source_name, d.description, dv.lifecycle_state,\s
          dv.created_at, dv.uuid AS current_version_uuid, dv.version, dv.dataset_schema_version_uuid, dv.fields, dv.run_uuid AS createdByRunUuid,
          sv.schema_location, t.tags, f.facets
      FROM selected_dataset_versions dv
      LEFT JOIN datasets_view d ON d.uuid = dv.dataset_uuid
      LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid
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
      """
      WITH dataset_info AS (
	    SELECT d.type, d.name, d.physical_name, d.namespace_name, d.source_name, d.description, dv.lifecycle_state,
		dv.created_at, dv.uuid AS current_version_uuid, dv.version, dv.dataset_schema_version_uuid, dv.fields, dv.run_uuid AS createdByRunUuid,
		sv.schema_location, t.tags, f.facets, f.lineage_event_time, f.dataset_version_uuid, facet_name
		FROM dataset_versions dv
		LEFT JOIN datasets_view d ON d.uuid = dv.dataset_uuid
		LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid
		LEFT JOIN (
			SELECT ARRAY_AGG(t.name) AS tags, m.dataset_uuid
			FROM tags AS t
			INNER JOIN datasets_tag_mapping AS m ON m.tag_uuid = t.uuid
			GROUP BY m.dataset_uuid
		) t ON t.dataset_uuid = dv.dataset_uuid
		LEFT JOIN (
			SELECT
				dataset_version_uuid,
				name as facet_name,
				facet as facets,lineage_event_time
			FROM dataset_facets_view
			WHERE
				(type ILIKE 'dataset' OR type ILIKE 'unknown' OR type ILIKE 'input')
      	) f ON f.dataset_version_uuid = dv.uuid
      	WHERE dv.namespace_name = :namespaceName
            AND dv.dataset_name = :datasetName
        ),
        dataset_symlinks_names as (
          SELECT DISTINCT dataset_uuid, name
          FROM dataset_symlinks
          WHERE NOT is_primary
        )
        SELECT
	        type, name, physical_name, namespace_name, source_name, description, lifecycle_state,
            created_at, current_version_uuid, version, dataset_schema_version_uuid, fields, createdByRunUuid, schema_location,
            tags, dataset_version_uuid,
	        JSONB_AGG(facets ORDER BY lineage_event_time ASC) AS facets
        FROM dataset_info
        WHERE name NOT IN (SELECT name FROM dataset_symlinks_names)
        GROUP BY type, name, physical_name, namespace_name, source_name, description, lifecycle_state,
            created_at, current_version_uuid, version, dataset_schema_version_uuid, fields, createdByRunUuid, schema_location,
            tags, dataset_version_uuid
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
  """)
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
      """
    select
        count(*)
    from
        dataset_versions
    where
        namespace_name = :namespaceName
    and
        dataset_name = :dataset
    ;
    """)
  int countDatasetVersions(String namespaceName, String dataset);

  @SqlQuery(
      "INSERT INTO dataset_versions "
          + "(uuid, created_at, dataset_uuid, version, dataset_schema_version_uuid, run_uuid, fields, namespace_name, dataset_name, lifecycle_state) "
          + "VALUES "
          + "(:uuid, :now, :datasetUuid, :version, :schemaVersionUuid, :runUuid, :fields, :namespaceName, :datasetName, :lifecycleState) "
          + "ON CONFLICT(version) "
          + "DO UPDATE SET "
          + "run_uuid = EXCLUDED.run_uuid "
          + "RETURNING *")
  DatasetVersionRow upsert(
      UUID uuid,
      Instant now,
      UUID datasetUuid,
      UUID version,
      UUID schemaVersionUuid,
      UUID runUuid,
      PGobject fields,
      String namespaceName,
      String datasetName,
      String lifecycleState);

  @SqlUpdate("UPDATE dataset_versions SET fields = :fields WHERE uuid = :uuid")
  void updateFields(UUID uuid, PGobject fields);
}
