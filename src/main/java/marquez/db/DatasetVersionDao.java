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

import static org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling.NULL_STRING;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import marquez.common.models.DatasetType;
import marquez.db.mappers.DatasetVersionRowMapper;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.StreamVersionRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(DatasetVersionRowMapper.class)
public interface DatasetVersionDao {
  @CreateSqlObject
  DatasetDao createDatasetDao();

  @CreateSqlObject
  DatasetFieldDao createDatasetFieldDao();

  @CreateSqlObject
  StreamVersionDao createStreamVersionDao();

  @Transaction
  default void insertWith(DatasetVersionRow row, List<DatasetFieldRow> fieldRows) {
    // Fields
    createDatasetFieldDao().insertAll(fieldRows);
    insert(row);
    // Version
    if (row instanceof StreamVersionRow) {
      createStreamVersionDao().insert((StreamVersionRow) row);
    }
    row.getFieldUuids().forEach(fieldUuid -> updateFields(row.getUuid(), fieldUuid));
    // Current
    final Instant updatedAt = row.getCreatedAt();
    createDatasetDao().updateVersion(row.getDatasetUuid(), updatedAt, row.getVersion());
  }

  @SqlUpdate(
      "INSERT INTO dataset_versions (uuid, created_at, dataset_uuid, version, run_uuid) "
          + "VALUES (:uuid, :createdAt, :datasetUuid, :version, :runUuid)")
  void insert(@BindBean DatasetVersionRow row);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM dataset_versions WHERE version = :version)")
  boolean exists(UUID version);

  @SqlUpdate(
      "INSERT INTO dataset_versions_field_mapping (dataset_version_uuid, dataset_field_uuid) "
          + "VALUES (:datasetVersionUuid, :datasetFieldUuid)")
  void updateFields(UUID datasetVersionUuid, UUID datasetFieldUuid);

  @SqlQuery(
      "SELECT *, "
          + "ARRAY(SELECT dataset_field_uuid "
          + "      FROM dataset_versions_field_mapping "
          + "      WHERE dataset_version_uuid = uuid) AS field_uuids "
          + "FROM dataset_versions "
          + "WHERE version = :version")
  Optional<DatasetVersionRow> findBy(UUID version);

  default Optional<DatasetVersionRow> find(String typeString, @Nullable UUID version) {
    if (version == null) {
      return Optional.empty();
    }

    final DatasetType type = DatasetType.valueOf(typeString);
    switch (type) {
      case STREAM:
        return createStreamVersionDao().findBy(version).map(DatasetVersionRow.class::cast);
      default:
        return findBy(version);
    }
  }

  @SqlQuery(
      "SELECT *, "
          + "ARRAY(SELECT dataset_field_uuid "
          + "      FROM dataset_versions_field_mapping "
          + "      WHERE dataset_version_uuid = uuid) AS field_uuids "
          + "FROM dataset_versions "
          + "WHERE dataset_uuid IN (<datasetUuids>)")
  List<DatasetVersionRow> findAllIn(@BindList(onEmpty = NULL_STRING) UUID... datasetUuids);

  @SqlQuery("SELECT COUNT(*) FROM dataset_versions")
  int count();
}
