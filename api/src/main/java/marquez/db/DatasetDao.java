/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.OpenLineageDao.DEFAULT_NAMESPACE_OWNER;
import static org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling.NULL_STRING;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Value;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.NamespaceName;
import marquez.common.models.TagName;
import marquez.db.mappers.DatasetMapper;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetSymlinkRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.db.models.TagRow;
import marquez.service.DatasetService;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DatasetRowMapper.class)
@RegisterRowMapper(DatasetMapper.class)
public interface DatasetDao extends BaseDao {
  @SqlQuery(
      "SELECT EXISTS ("
          + "SELECT 1 FROM datasets_view AS d "
          + "WHERE d.name = :datasetName AND d.namespace_name = :namespaceName)")
  boolean exists(String namespaceName, String datasetName);

  @SqlBatch(
      "INSERT INTO datasets_tag_mapping (dataset_uuid, tag_uuid, tagged_at) "
          + "VALUES (:rowUuid, :tagUuid, :taggedAt) "
          + "ON CONFLICT DO NOTHING")
  void updateTagMapping(@BindBean List<DatasetTagMapping> datasetTagMappings);

  @SqlUpdate(
      "UPDATE datasets "
          + "SET updated_at = :lastModifiedAt, "
          + "    last_modified_at = :lastModifiedAt "
          + "WHERE uuid IN (<rowUuids>)")
  void updateLastModifiedAt(
      @BindList(onEmpty = NULL_STRING) List<UUID> rowUuids, Instant lastModifiedAt);

  @SqlUpdate(
      "UPDATE datasets "
          + "SET updated_at = :updatedAt, "
          + "    current_version_uuid = :currentVersionUuid "
          + "WHERE uuid = :rowUuid")
  void updateVersion(UUID rowUuid, Instant updatedAt, UUID currentVersionUuid);

  @SqlQuery(
      """
      SELECT d.*, dv.fields, dv.lifecycle_state, sv.schema_location, t.tags, facets
      FROM datasets_view d
      LEFT JOIN dataset_versions dv ON d.current_version_uuid = dv.uuid
      LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid
      LEFT JOIN (
          SELECT ARRAY_AGG(t.name) AS tags, m.dataset_uuid
          FROM tags AS t
                   INNER JOIN datasets_tag_mapping AS m ON m.tag_uuid = t.uuid
          GROUP BY m.dataset_uuid
      ) t ON t.dataset_uuid = d.uuid
      LEFT JOIN (
          SELECT
              df.dataset_version_uuid,
              JSONB_AGG(df.facet ORDER BY df.lineage_event_time ASC) AS facets
          FROM dataset_facets_view AS df
          WHERE df.facet IS NOT NULL AND (df.type ILIKE 'dataset' OR df.type ILIKE 'unknown')
          GROUP BY df.dataset_version_uuid
      ) f ON f.dataset_version_uuid = d.current_version_uuid
      WHERE CAST((:namespaceName, :datasetName) AS DATASET_NAME) = ANY(d.dataset_symlinks)
  """)
  Optional<Dataset> findDatasetByName(String namespaceName, String datasetName);

  default Optional<Dataset> findWithTags(String namespaceName, String datasetName) {
    Optional<Dataset> dataset = findDatasetByName(namespaceName, datasetName);
    dataset.ifPresent(this::setFields);
    return dataset;
  }

  default void setFields(Dataset ds) {
    DatasetFieldDao datasetFieldDao = createDatasetFieldDao();

    ds.getCurrentVersion()
        .ifPresent(
            dsv -> {
              ds.setFields(datasetFieldDao.find(dsv));
            });
  }

  @SqlQuery(
      "SELECT d.* FROM datasets_view AS d WHERE d.name = :datasetName AND d.namespace_name = :namespaceName")
  Optional<DatasetRow> findDatasetAsRow(String namespaceName, String datasetName);

  @SqlQuery(
      "SELECT * FROM datasets_view WHERE name = :datasetName AND namespace_name = :namespaceName")
  Optional<DatasetRow> getUuid(String namespaceName, String datasetName);

  @SqlQuery(
      """
      SELECT d.*, dv.fields, dv.lifecycle_state, sv.schema_location, t.tags, facets
      FROM datasets_view d
      LEFT JOIN dataset_versions dv ON d.current_version_uuid = dv.uuid
      LEFT JOIN stream_versions AS sv ON sv.dataset_version_uuid = dv.uuid
      LEFT JOIN (
          SELECT ARRAY_AGG(t.name) AS tags, m.dataset_uuid
          FROM tags AS t
          INNER JOIN datasets_tag_mapping AS m ON m.tag_uuid = t.uuid
          GROUP BY m.dataset_uuid
      ) t ON t.dataset_uuid = d.uuid
      LEFT JOIN (
          SELECT
              df.dataset_version_uuid,
              JSONB_AGG(df.facet ORDER BY df.lineage_event_time ASC) AS facets
          FROM dataset_facets_view AS df
          WHERE df.facet IS NOT NULL AND (df.type ILIKE 'dataset' OR df.type ILIKE 'unknown')
          GROUP BY df.dataset_version_uuid
      ) f ON f.dataset_version_uuid = d.current_version_uuid
      WHERE d.namespace_name = :namespaceName
      ORDER BY d.name
      LIMIT :limit OFFSET :offset
      """)
  List<Dataset> findAll(String namespaceName, int limit, int offset);

  @SqlQuery("SELECT count(*) FROM datasets_view")
  int count();

  @SqlQuery("SELECT count(*) FROM datasets_view AS j WHERE j.namespace_name = :namespaceName")
  int countFor(String namespaceName);

  default List<Dataset> findAllWithTags(String namespaceName, int limit, int offset) {
    List<Dataset> datasets = findAll(namespaceName, limit, offset);
    return datasets.stream().peek(this::setFields).collect(Collectors.toList());
  }

  @SqlQuery(
      """
      INSERT INTO datasets (
          uuid,
          type,
          created_at,
          updated_at,
          namespace_uuid,
          namespace_name,
          source_uuid,
          source_name,
          name,
          physical_name,
          description,
          is_deleted,
          is_hidden
          ) VALUES (
            :uuid,
            :type,
            :now,
            :now,
            :namespaceUuid,
            :namespaceName,
            :sourceUuid,
            :sourceName,
            :name,
            :physicalName,
            :description,
            :isDeleted,
            false
          ) ON CONFLICT (uuid)
          DO UPDATE SET
          type = EXCLUDED.type,
          updated_at = EXCLUDED.updated_at,
          physical_name = EXCLUDED.physical_name,
          description = EXCLUDED.description,
          is_deleted = EXCLUDED.is_deleted,
          is_hidden = EXCLUDED.is_hidden
          RETURNING *
    """)
  DatasetRow upsert(
      UUID uuid,
      DatasetType type,
      Instant now,
      UUID namespaceUuid,
      String namespaceName,
      UUID sourceUuid,
      String sourceName,
      String name,
      String physicalName,
      String description,
      boolean isDeleted);

  @SqlQuery(
      "INSERT INTO datasets ("
          + "uuid, "
          + "type, "
          + "created_at, "
          + "updated_at, "
          + "namespace_uuid, "
          + "namespace_name, "
          + "source_uuid, "
          + "source_name, "
          + "name, "
          + "physical_name "
          + ") VALUES ( "
          + ":uuid, "
          + ":type, "
          + ":now, "
          + ":now, "
          + ":namespaceUuid, "
          + ":namespaceName, "
          + ":sourceUuid, "
          + ":sourceName, "
          + ":name, "
          + ":physicalName) "
          + "ON CONFLICT (uuid) "
          + "DO UPDATE SET "
          + "type = EXCLUDED.type, "
          + "updated_at = EXCLUDED.updated_at, "
          + "physical_name = EXCLUDED.physical_name "
          + "RETURNING *")
  DatasetRow upsert(
      UUID uuid,
      DatasetType type,
      Instant now,
      UUID namespaceUuid,
      String namespaceName,
      UUID sourceUuid,
      String sourceName,
      String name,
      String physicalName);

  @SqlUpdate(
      """
    UPDATE datasets d
    SET is_hidden = true
    FROM namespaces n
    WHERE n.uuid=d.namespace_uuid
    AND n.name=:namespaceName
  """)
  void deleteByNamespaceName(String namespaceName);

  @SqlQuery(
      """
    UPDATE datasets d
    SET is_hidden = true
    FROM namespaces n
    WHERE n.uuid = d.namespace_uuid
    AND n.name=:namespaceName AND d.name=:name
    RETURNING *
  """)
  Optional<DatasetRow> delete(String namespaceName, String name);

  @Transaction
  default Dataset upsertDatasetMeta(
      NamespaceName namespaceName, DatasetName datasetName, DatasetMeta datasetMeta) {
    Instant now = Instant.now();
    NamespaceRow namespaceRow =
        createNamespaceDao()
            .upsertNamespaceRow(
                UUID.randomUUID(), now, namespaceName.getValue(), DEFAULT_NAMESPACE_OWNER);
    DatasetSymlinkRow symlinkRow =
        createDatasetSymlinkDao()
            .upsertDatasetSymlinkRow(
                UUID.randomUUID(), datasetName.getValue(), namespaceRow.getUuid(), true, null, now);
    SourceRow sourceRow =
        createSourceDao()
            .upsertOrDefault(
                UUID.randomUUID(),
                toDefaultSourceType(datasetMeta.getType()),
                now,
                datasetMeta.getSourceName().getValue(),
                "");
    DatasetRow datasetRow;

    if (datasetMeta.getDescription().isPresent()) {
      datasetRow =
          upsert(
              symlinkRow.getUuid(),
              datasetMeta.getType(),
              now,
              namespaceRow.getUuid(),
              namespaceRow.getName(),
              sourceRow.getUuid(),
              sourceRow.getName(),
              datasetName.getValue(),
              datasetMeta.getPhysicalName().getValue(),
              datasetMeta.getDescription().orElse(null),
              false);
    } else {
      datasetRow =
          upsert(
              symlinkRow.getUuid(),
              datasetMeta.getType(),
              now,
              namespaceRow.getUuid(),
              namespaceRow.getName(),
              sourceRow.getUuid(),
              sourceRow.getName(),
              datasetName.getValue(),
              datasetMeta.getPhysicalName().getValue());
    }

    updateDatasetMetric(
        namespaceName, datasetMeta.getType(), symlinkRow.getUuid(), datasetRow.getUuid());

    TagDao tagDao = createTagDao();
    List<DatasetTagMapping> datasetTagMappings = new ArrayList<>();
    for (TagName tagName : datasetMeta.getTags()) {
      TagRow tag = tagDao.upsert(UUID.randomUUID(), now, tagName.getValue());
      datasetTagMappings.add(new DatasetTagMapping(datasetRow.getUuid(), tag.getUuid(), now));
    }
    updateTagMapping(datasetTagMappings);

    DatasetVersionRow dvRow =
        createDatasetVersionDao()
            .upsertDatasetVersion(
                datasetRow.getUuid(),
                now,
                namespaceName.getValue(),
                datasetName.getValue(),
                null,
                datasetMeta);

    return findWithTags(namespaceName.getValue(), datasetName.getValue()).get();
  }

  default String toDefaultSourceType(DatasetType type) {
    return "POSTGRES";
  }

  default void updateDatasetMetric(
      NamespaceName namespaceName,
      DatasetType datasetType,
      UUID newDatasetUuid,
      UUID currentDatasetUuid) {
    if (newDatasetUuid != currentDatasetUuid) {
      DatasetService.datasets.labels(namespaceName.getValue(), datasetType.toString()).inc();
    }
  }

  default Dataset updateTags(String namespaceName, String datasetName, String tagName) {
    Instant now = Instant.now();
    DatasetRow datasetRow = findDatasetAsRow(namespaceName, datasetName).get();
    TagRow tagRow = createTagDao().upsert(UUID.randomUUID(), now, tagName);
    updateTagMapping(
        ImmutableList.of(new DatasetTagMapping(datasetRow.getUuid(), tagRow.getUuid(), now)));
    return findDatasetByName(namespaceName, datasetName).get();
  }

  @Value
  class DatasetTagMapping {
    UUID rowUuid;
    UUID tagUuid;
    Instant taggedAt;
  }
}
