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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.TagName;
import marquez.common.models.Version;
import marquez.db.DatasetFieldDao.DatasetFieldMapping;
import marquez.db.DatasetFieldDao.DatasetFieldTag;
import marquez.db.mappers.DatasetVersionRowMapper;
import marquez.db.mappers.ExtendedDatasetVersionRowMapper;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.TagRow;
import marquez.service.DatasetService;
import marquez.service.models.DatasetMeta;
import marquez.service.models.StreamMeta;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DatasetVersionRowMapper.class)
@RegisterRowMapper(ExtendedDatasetVersionRowMapper.class)
public interface DatasetVersionDao extends MarquezDao {

  @Transaction
  default void upsertDatasetVersion(
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
            datasetMeta.getRunId().map(RunId::getValue).orElse(null));
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

  String SELECT =
      "SELECT dv.*, "
          + "ARRAY(SELECT dataset_field_uuid "
          + "      FROM dataset_versions_field_mapping "
          + "      WHERE dataset_version_uuid = dv.uuid) AS field_uuids "
          + "FROM dataset_versions dv ";

  String EXTENDED_SELECT =
      "SELECT dv.*, d.name as dataset_name, n.name as namespace_name, "
          + "ARRAY(SELECT dataset_field_uuid "
          + "      FROM dataset_versions_field_mapping "
          + "      WHERE dataset_version_uuid = dv.uuid) AS field_uuids "
          + "FROM dataset_versions AS dv "
          + "INNER JOIN datasets AS d ON d.uuid = dv.dataset_uuid "
          + "INNER JOIN namespaces AS n ON n.uuid = d.namespace_uuid ";

  @SqlQuery(SELECT + "WHERE uuid = :uuid")
  Optional<DatasetVersionRow> findBy(UUID uuid);

  default Optional<DatasetVersionRow> find(String typeString, @Nullable UUID uuid) {
    if (uuid == null) {
      return Optional.empty();
    }

    final DatasetType type = DatasetType.valueOf(typeString);
    switch (type) {
      case STREAM:
        return createStreamVersionDao().findBy(uuid).map(DatasetVersionRow.class::cast);
      default:
        return findBy(uuid);
    }
  }

  /**
   * returns all Dataset Versions created by this run id
   *
   * @param runId - the run ID
   */
  @SqlQuery(EXTENDED_SELECT + " WHERE run_uuid = :runId")
  List<ExtendedDatasetVersionRow> findByRunId(@NonNull UUID runId);

  @SqlQuery(
      EXTENDED_SELECT
          + " INNER JOIN runs_input_mapping m ON m.dataset_version_uuid = dv.uuid WHERE m.run_uuid = :runUuid")
  List<ExtendedDatasetVersionRow> findInputsByRunId(UUID runUuid);

  @SqlQuery(
      EXTENDED_SELECT
          + "WHERE n.name = :namespaceName AND d.name = :datasetName "
          + "ORDER BY created_at DESC "
          + "LIMIT :limit OFFSET :offset")
  @RegisterRowMapper(ExtendedDatasetVersionRowMapper.class)
  List<ExtendedDatasetVersionRow> findAll(
      String namespaceName, String datasetName, int limit, int offset);

  @SqlQuery(
      "INSERT INTO dataset_versions "
          + "(uuid, created_at, dataset_uuid, version, run_uuid) "
          + "VALUES "
          + "(:uuid, :now, :datasetUuid, :version, :runUuid) "
          + "ON CONFLICT(version) "
          + "DO UPDATE SET "
          + "run_uuid = EXCLUDED.run_uuid "
          + "RETURNING *")
  DatasetVersionRow upsert(UUID uuid, Instant now, UUID datasetUuid, UUID version, UUID runUuid);
}
