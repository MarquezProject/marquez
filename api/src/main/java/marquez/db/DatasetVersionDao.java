/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.FieldName;
import marquez.common.models.FieldType;
import marquez.common.models.NamespaceName;
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
      DatasetMeta datasetMeta) {
    TagDao tagDao = createTagDao();
    DatasetFieldDao datasetFieldDao = createDatasetFieldDao();

    final Version version =
        datasetMeta.version(NamespaceName.of(namespaceName), DatasetName.of(datasetName));
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
            datasetName);
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
              field.getType().name(),
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
                    FieldName.of(f.getName()),
                    openLineageDao.toFieldType(f.getType()) != null
                        ? FieldType.valueOf(openLineageDao.toFieldType(f.getType()))
                        : FieldType.VARCHAR,
                    ImmutableSet.of(),
                    f.getDescription()))
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

  String BASE_DATASET_VERSION_SELECT =
      "SELECT d.type, d.name, d.physical_name, dv.namespace_name, d.source_name, d.description, "
          + "dv.created_at, dv.version, dv.fields, dv.run_uuid AS \"createdByRunUuid\", sv.schema_location, "
          + "ARRAY(SELECT t.name FROM tags AS t "
          + "       INNER JOIN datasets_tag_mapping AS m ON m.tag_uuid = t.uuid "
          + "       WHERE d.uuid = m.dataset_uuid) AS tags, "
          + "(SELECT JSON_AGG(facets_by_event.facets) "
          + "   FROM ("
          + "      (SELECT JSONB_ARRAY_ELEMENTS(event->'inputs')->'facets' AS facets "
          + "         FROM lineage_events AS le "
          + "        WHERE le.run_id = dv.run_uuid::text "
          + "        ORDER BY event_time ASC) "
          + "      UNION "
          + "      (SELECT JSONB_ARRAY_ELEMENTS(event->'outputs')->'facets' AS facets "
          + "         FROM lineage_events AS le "
          + "        WHERE le.run_id = dv.run_uuid::text "
          + "        ORDER BY event_time ASC) "
          + "   ) AS facets_by_event "
          + ") AS facets "
          + "FROM datasets AS d "
          + "INNER JOIN dataset_versions dv ON d.uuid = dv.dataset_uuid "
          + "LEFT OUTER JOIN stream_versions sv ON sv.dataset_version_uuid = d.current_version_uuid ";

  @SqlQuery(BASE_DATASET_VERSION_SELECT + "WHERE dv.version = :version")
  Optional<DatasetVersion> findBy(UUID version);

  @SqlQuery(BASE_DATASET_VERSION_SELECT + "WHERE dv.uuid = :uuid")
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
      BASE_DATASET_VERSION_SELECT
          + "WHERE dv.namespace_name = :namespaceName AND dv.dataset_name = :datasetName "
          + "ORDER BY dv.created_at DESC "
          + "LIMIT :limit OFFSET :offset")
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
          + "(uuid, created_at, dataset_uuid, version, run_uuid, fields, namespace_name, dataset_name) "
          + "VALUES "
          + "(:uuid, :now, :datasetUuid, :version, :runUuid, :fields, :namespaceName, :datasetName) "
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
      String datasetName);

  @SqlUpdate("UPDATE dataset_versions SET fields = :fields WHERE uuid = :uuid")
  void updateFields(UUID uuid, PGobject fields);
}
