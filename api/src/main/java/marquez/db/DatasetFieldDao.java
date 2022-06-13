/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Value;
import marquez.common.models.Field;
import marquez.common.models.TagName;
import marquez.db.mappers.DatasetFieldMapper;
import marquez.db.mappers.DatasetFieldRowMapper;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.TagRow;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetVersion;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DatasetFieldRowMapper.class)
@RegisterRowMapper(DatasetFieldMapper.class)
public interface DatasetFieldDao extends BaseDao {
  @SqlQuery(
      "SELECT EXISTS ("
          + "SELECT 1 FROM dataset_fields AS df "
          + "INNER JOIN datasets AS d "
          + "  ON d.uuid = df.dataset_uuid AND d.name = :datasetName AND d.namespace_name = :namespaceName "
          + "WHERE df.name = :name)")
  boolean exists(String namespaceName, String datasetName, String name);

  default Dataset updateTags(
      String namespaceName, String datasetName, String fieldName, String tagName) {
    Instant now = Instant.now();
    TagRow tag = createTagDao().upsert(UUID.randomUUID(), now, tagName);
    DatasetRow datasetRow = createDatasetDao().getUuid(namespaceName, datasetName).get();
    UUID fieldUuid = createDatasetFieldDao().findUuid(datasetRow.getUuid(), fieldName).get();
    datasetRow
        .getCurrentVersionUuid()
        .ifPresent(
            ver -> {
              DatasetVersionDao datasetVersionDao = createDatasetVersionDao();
              DatasetVersion dsVer = datasetVersionDao.findByUuid(ver).get();
              List<Field> fields =
                  dsVer.getFields().stream()
                      .map(
                          f -> {
                            if (f.getName().getValue().equalsIgnoreCase(fieldName)) {
                              ImmutableSet<TagName> tags =
                                  ImmutableSet.<TagName>builder()
                                      .addAll(f.getTags())
                                      .add(TagName.of(tagName))
                                      .build();
                              return new Field(
                                  f.getName(), f.getType(), tags, f.getDescription().orElse(null));
                            } else {
                              return f;
                            }
                          })
                      .collect(Collectors.toList());
              datasetVersionDao.updateFields(ver, datasetVersionDao.toPgObjectFields(fields));
            });

    updateTags(fieldUuid, tag.getUuid(), now);
    return createDatasetDao().findDatasetByName(namespaceName, datasetName).get();
  }

  @SqlUpdate(
      "INSERT INTO dataset_fields_tag_mapping (dataset_field_uuid, tag_uuid, tagged_at) "
          + "VALUES (:rowUuid, :tagUuid, :taggedAt)")
  void updateTags(UUID rowUuid, UUID tagUuid, Instant taggedAt);

  @SqlBatch(
      "INSERT INTO dataset_fields_tag_mapping (dataset_field_uuid, tag_uuid, tagged_at) "
          + "VALUES (:datasetFieldUuid, :tagUuid, :taggedAt) ON CONFLICT DO NOTHING")
  void updateTags(@BindBean List<DatasetFieldTag> datasetFieldTag);

  @SqlQuery(
      "SELECT uuid "
          + "FROM dataset_fields "
          + "WHERE dataset_uuid = :datasetUuid AND name = :name")
  Optional<UUID> findUuid(UUID datasetUuid, String name);

  @SqlQuery(
      "SELECT f.*, "
          + "ARRAY(SELECT t.name "
          + "      FROM dataset_fields_tag_mapping m "
          + "      INNER JOIN tags t on t.uuid = m.tag_uuid "
          + "      WHERE m.dataset_field_uuid = f.uuid) AS tags "
          + "FROM dataset_fields f "
          + "INNER JOIN dataset_versions_field_mapping fm on fm.dataset_field_uuid = f.uuid "
          + "WHERE fm.dataset_version_uuid = :datasetVersionUuid")
  List<Field> find(UUID datasetVersionUuid);

  @SqlQuery(
      "INSERT INTO dataset_fields ("
          + "uuid, "
          + "type, "
          + "created_at, "
          + "updated_at, "
          + "dataset_uuid, "
          + "name, "
          + "description"
          + ") VALUES ("
          + ":uuid, "
          + ":type, "
          + ":now, "
          + ":now, "
          + ":datasetUuid, "
          + ":name, "
          + ":description) "
          + "ON CONFLICT(dataset_uuid, name, type) "
          + "DO UPDATE SET "
          + "updated_at = EXCLUDED.updated_at, "
          + "description = EXCLUDED.description "
          + "RETURNING *")
  DatasetFieldRow upsert(
      UUID uuid, Instant now, String name, String type, String description, UUID datasetUuid);

  @SqlBatch(
      "INSERT INTO dataset_versions_field_mapping (dataset_version_uuid, dataset_field_uuid) "
          + "VALUES (:datasetVersionUuid, :datasetFieldUuid) ON CONFLICT DO NOTHING")
  void updateFieldMapping(@BindBean List<DatasetFieldMapping> datasetFieldMappings);

  @Value
  class DatasetFieldMapping {
    UUID datasetVersionUuid;
    UUID datasetFieldUuid;
  }

  @Value
  class DatasetFieldTag {
    UUID datasetFieldUuid;
    UUID tagUuid;
    Instant taggedAt;
  }
}
