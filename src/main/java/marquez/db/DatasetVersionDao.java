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

import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetType;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.StreamVersionRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

public interface DatasetVersionDao {
  @CreateSqlObject
  DatasetDao createDatasetDao();

  @CreateSqlObject
  StreamVersionDao createStreamVersionDao();

  @Transaction
  default void insertAndUpdate(DatasetVersionRow row) {
    if (row instanceof StreamVersionRow) {
      insert(row);
      createStreamVersionDao().insert((StreamVersionRow) row);
      createDatasetDao().update(row.getDatasetUuid(), row.getCreatedAt(), row.getVersion());
    }
  }

  @SqlUpdate(
      "INSERT INTO dataset_versions (uuid, created_at, dataset_uuid, version, run_uuid) "
          + "VALUES (:uuid, :createdAt, :datasetUuid, :version, :runUuid)")
  void insert(@BindBean DatasetVersionRow row);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM dataset_versions WHERE version = :version)")
  boolean exists(UUID version);

  default Optional<DatasetVersionRow> findBy(String typeString, UUID version) {
    final DatasetType type = DatasetType.valueOf(typeString);
    switch (type) {
      case STREAM:
        return createStreamVersionDao().findBy(version).map(DatasetVersionRow.class::cast);
      default:
        return Optional.empty();
    }
  }

  @SqlQuery("SELECT COUNT(*) FROM dataset_versions")
  int count();
}
