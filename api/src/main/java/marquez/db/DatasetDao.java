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

import static marquez.db.OpenLineageDao.DEFAULT_NAMESPACE_OWNER;
import static org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling.NULL_STRING;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Value;
import marquez.common.models.DatasetType;
import marquez.common.models.TagName;
import marquez.db.mappers.DatasetMapper;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.models.DatasetRow;
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
          + "SELECT 1 FROM datasets AS d "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.uuid = d.namespace_uuid AND n.name = :namespaceName) "
          + "WHERE d.name = :datasetName)")
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

  String TAG_UUIDS =
      "ARRAY(SELECT tag_uuid "
          + "      FROM datasets_tag_mapping "
          + "      WHERE dataset_uuid = d.uuid) AS tag_uuids ";

  String SELECT = "SELECT d.*, " + TAG_UUIDS + "FROM datasets AS d ";

  String DATASET_SELECT =
      "select d.*, t_json.fields, ARRAY(select t.name from tags t\n"
          + "    inner join datasets_tag_mapping m on m.tag_uuid = t.uuid\n"
          + "    where d.uuid = m.dataset_uuid) as tags,"
          + "    sv.schema_location\n"
          + "from datasets d\n"
          + "left outer join stream_versions sv on sv.dataset_version_uuid = d.current_version_uuid\n"
          + "left outer join (\n"
          + "    select fm.dataset_version_uuid, jsonb_agg((select x from (select f.name, f.type, f.description, t_agg.agg as tags) as x) ) as fields\n"
          + "    from dataset_fields f\n"
          + "    inner join dataset_versions_field_mapping fm on fm.dataset_field_uuid = f.uuid\n"
          + "    left outer join (select m.dataset_field_uuid, jsonb_agg((select t.name)) agg\n"
          + "        from dataset_fields_tag_mapping m\n"
          + "        inner join tags t on m.tag_uuid = t.uuid\n"
          + "        group by m.dataset_field_uuid\n"
          + "        ) t_agg on t_agg.dataset_field_uuid = f.uuid\n"
          + "    group by fm.dataset_version_uuid) t_json on t_json.dataset_version_uuid = d.current_version_uuid\n";

  @SqlQuery(DATASET_SELECT + " WHERE d.name = :datasetName AND d.namespace_name = :namespaceName")
  Optional<Dataset> find(String namespaceName, String datasetName);

  @SqlQuery(SELECT + " WHERE d.name = :datasetName AND d.namespace_name = :namespaceName")
  Optional<DatasetRow> findByRow(String namespaceName, String datasetName);

  @SqlQuery(
      "SELECT uuid FROM datasets WHERE name = :datasetName AND namespace_name = :namespaceName")
  Optional<UUID> getUuid(String namespaceName, String datasetName);

  @SqlQuery(
      DATASET_SELECT
          + "WHERE d.namespace_name = :namespaceName "
          + "ORDER BY d.name "
          + "LIMIT :limit OFFSET :offset")
  List<Dataset> findAll(String namespaceName, int limit, int offset);

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
          + "physical_name, "
          + "description "
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
          + ":physicalName, "
          + ":description) "
          + "ON CONFLICT (namespace_uuid, name) "
          + "DO UPDATE SET "
          + "type = EXCLUDED.type, "
          + "updated_at = EXCLUDED.updated_at, "
          + "physical_name = EXCLUDED.physical_name, "
          + "description = EXCLUDED.description "
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
      String physicalName,
      String description);

  @Transaction
  default Dataset upsertDatasetMeta(
      String namespaceName, String datasetName, DatasetMeta datasetMeta) {
    Instant now = Instant.now();
    NamespaceRow namespaceRow =
        createNamespaceDao().upsert(UUID.randomUUID(), now, namespaceName, DEFAULT_NAMESPACE_OWNER);
    SourceRow sourceRow =
        createSourceDao()
            .upsertOrDefault(
                UUID.randomUUID(),
                toDefaultSourceType(datasetMeta.getType()),
                now,
                datasetMeta.getSourceName().getValue(),
                "");
    UUID newDatasetUuid = UUID.randomUUID();
    DatasetRow datasetRow =
        upsert(
            newDatasetUuid,
            datasetMeta.getType(),
            now,
            namespaceRow.getUuid(),
            namespaceRow.getName(),
            sourceRow.getUuid(),
            sourceRow.getName(),
            datasetName,
            datasetMeta.getPhysicalName().getValue(),
            datasetMeta.getDescription().orElse(null));
    updateDatasetMetric(
        namespaceName, datasetMeta.getType().toString(), newDatasetUuid, datasetRow.getUuid());

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
                datasetRow.getUuid(), now, namespaceName, datasetName, datasetMeta);

    return find(namespaceName, datasetName).get();
  }

  default String toDefaultSourceType(DatasetType type) {
    return "POSTGRES";
  }

  default void updateDatasetMetric(
      String namespaceName, String type, UUID newDatasetUuid, UUID datasetUuid) {
    if (newDatasetUuid != datasetUuid) {
      DatasetService.datasets.labels(namespaceName, type).inc();
    }
  }

  default Dataset updateTags(String namespaceName, String datasetName, String tagName) {
    Instant now = Instant.now();
    DatasetRow datasetRow = findByRow(namespaceName, datasetName).get();
    TagRow tagRow = createTagDao().upsert(UUID.randomUUID(), now, tagName);
    updateTagMapping(
        ImmutableList.of(new DatasetTagMapping(datasetRow.getUuid(), tagRow.getUuid(), now)));
    return find(namespaceName, datasetName).get();
  }

  @Value
  class DatasetTagMapping {
    UUID rowUuid;
    UUID tagUuid;
    Instant taggedAt;
  }
}
