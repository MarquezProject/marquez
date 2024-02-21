/*
 * Copyright 2018-2023 contributors to the Marquez project
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
import marquez.db.mappers.FieldDataMapper;
import marquez.db.mappers.PairUuidInstantMapper;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.InputFieldData;
import marquez.db.models.TagRow;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetVersion;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DatasetFieldRowMapper.class)
@RegisterRowMapper(DatasetFieldMapper.class)
@RegisterRowMapper(FieldDataMapper.class)
@RegisterRowMapper(PairUuidInstantMapper.class)
public interface DatasetFieldDao extends BaseDao {
  @SqlQuery(
      """
          SELECT EXISTS (
            SELECT 1 FROM dataset_fields AS df
            INNER JOIN datasets_view AS d ON d.uuid = df.dataset_uuid
            WHERE CAST((:namespaceName, :datasetName) AS DATASET_NAME) = ANY(d.dataset_symlinks)
            AND df.name = :name
          )
      """)
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
      """
      DELETE FROM dataset_fields_tag_mapping dftm
      WHERE EXISTS
      (
          SELECT 1
          FROM
              dataset_fields df
          JOIN datasets d ON df.dataset_uuid = d.uuid AND df.uuid = dftm.dataset_field_uuid
          JOIN tags t ON dftm.tag_uuid = t.uuid
          JOIN namespaces n ON d.namespace_uuid = n.uuid
          WHERE d.name = :datasetName
          AND t.name = :tagName
          AND n.name = :namespaceName
          AND df.name = :fieldName
      );
    """)
  void deleteDatasetFieldTag(
      String namespaceName, String datasetName, String fieldName, String tagName);

  @SqlUpdate(
      """
        UPDATE dataset_versions
        SET fields = (
            SELECT jsonb_agg(
                CASE
                    WHEN elem->>'name' = :fieldName AND elem->'tags' @> jsonb_build_array(:tagName)
                    THEN jsonb_set(elem, '{tags}', (elem->'tags') - :tagName)
                    ELSE elem
                END
            )
            FROM jsonb_array_elements(fields) AS elem
        )
        WHERE dataset_name = :datasetName AND namespace_name = :namespaceName
        AND
        EXISTS (
            SELECT 1
            FROM jsonb_array_elements(fields) AS elem
            WHERE elem->>'name' = :fieldName
            AND elem->'tags' @> jsonb_build_array(:tagName)
        )
    """)
  void deleteDatasetVersionFieldTag(
      String namespaceName, String datasetName, String fieldName, String tagName);

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
      """
          SELECT df.uuid, max(dv.created_at)
          FROM dataset_fields df
          JOIN datasets_view AS d ON d.uuid = df.dataset_uuid
          JOIN dataset_versions_field_mapping AS fm ON fm.dataset_field_uuid = df.uuid
          JOIN dataset_versions AS dv ON dv.uuid = fm.dataset_version_uuid
          WHERE CAST((:namespaceName, :datasetName) AS DATASET_NAME) = ANY(d.dataset_symlinks)
          GROUP BY df.uuid
      """)
  List<UUID> findDatasetFieldsUuids(String namespaceName, String datasetName);

  @SqlQuery(
      """
          SELECT df.uuid, dv.created_at
          FROM dataset_fields  df
          JOIN dataset_versions_field_mapping AS fm ON fm.dataset_field_uuid = df.uuid
          JOIN dataset_versions AS dv ON dv.uuid = :datasetVersion
          WHERE fm.dataset_version_uuid = :datasetVersion
      """)
  List<Pair<UUID, Instant>> findDatasetVersionFieldsUuids(UUID datasetVersion);

  @SqlQuery(
      """
          WITH latest_run AS (
            SELECT DISTINCT r.uuid as uuid, r.created_at
            FROM runs_view r
            WHERE r.namespace_name = :namespaceName AND r.job_name = :jobName
            ORDER BY r.created_at DESC
            LIMIT 1
          )
          SELECT dataset_fields.uuid
          FROM dataset_fields
          JOIN dataset_versions ON dataset_versions.dataset_uuid = dataset_fields.dataset_uuid
          JOIN latest_run ON dataset_versions.run_uuid = latest_run.uuid
      """)
  List<UUID> findFieldsUuidsByJob(String namespaceName, String jobName);

  @SqlQuery(
      """
          SELECT dataset_fields.uuid, r.created_at
          FROM dataset_fields
          JOIN dataset_versions ON dataset_versions.dataset_uuid = dataset_fields.dataset_uuid
          JOIN runs_view r ON r.job_version_uuid = :jobVersion
      """)
  List<Pair<UUID, Instant>> findFieldsUuidsByJobVersion(UUID jobVersion);

  @SqlQuery(
      """
          SELECT df.uuid
          FROM dataset_fields  df
          JOIN datasets_view AS d ON d.uuid = df.dataset_uuid
          WHERE CAST((:namespaceName, :datasetName) AS DATASET_NAME) = ANY(d.dataset_symlinks)
          AND df.name = :name
      """)
  Optional<UUID> findUuid(String namespaceName, String datasetName, String name);

  @SqlQuery(
      """
          SELECT df.uuid, dv.created_at
          FROM dataset_fields  df
          JOIN datasets_view AS d ON d.uuid = df.dataset_uuid
          JOIN dataset_versions AS dv ON dv.uuid = :datasetVersion
          JOIN dataset_versions_field_mapping AS fm ON fm.dataset_field_uuid = df.uuid
          WHERE fm.dataset_version_uuid = :datasetVersion AND df.name = :fieldName
      """)
  List<Pair<UUID, Instant>> findDatasetVersionFieldsUuids(String fieldName, UUID datasetVersion);

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
      """
          SELECT
            datasets_view.namespace_name as namespace_name,
            datasets_view.name as dataset_name,
            dataset_fields.name as field_name,
            datasets_view.uuid as dataset_uuid,
            dataset_versions.uuid as dataset_version_uuid,
            dataset_fields.uuid as dataset_field_uuid
          FROM dataset_fields
          JOIN dataset_versions_field_mapping fm ON fm.dataset_field_uuid = dataset_fields.uuid
          JOIN dataset_versions ON dataset_versions.uuid = fm.dataset_version_uuid
          JOIN datasets_view ON datasets_view.uuid = dataset_versions.dataset_uuid
          JOIN runs_input_mapping ON runs_input_mapping.dataset_version_uuid =  dataset_versions.uuid
          WHERE runs_input_mapping.run_uuid = :runUuid
          """)
  List<InputFieldData> findInputFieldsDataAssociatedWithRun(UUID runUuid);

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
