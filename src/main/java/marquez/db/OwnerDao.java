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
import marquez.db.mappers.OwnerRowMapper;
import marquez.db.models.NamespaceOwnershipRow;
import marquez.db.models.OwnerRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(OwnerRowMapper.class)
public interface OwnerDao {
  @CreateSqlObject
  NamespaceDao createNamespaceDao();

  @CreateSqlObject
  NamespaceOwnershipDao createNamespaceOwnershipDao();

  @Transaction
  default void insertAndUpdateWith(OwnerRow row, NamespaceOwnershipRow ownershipRow) {
    createNamespaceOwnershipDao()
        .update(ownershipRow.getStartedAt(), ownershipRow.getNamespaceUuid(), row.getUuid());
    insertAndUpdateWith(row, ownershipRow.getNamespaceUuid());
    createNamespaceOwnershipDao().insert(ownershipRow);
  }

  @Transaction
  default void insertAndUpdateWith(OwnerRow row, UUID namespaceUuid) {
    insert(row);
    createNamespaceDao().update(namespaceUuid, row.getCreatedAt(), row.getName());
  }

  @SqlUpdate("INSERT INTO owners (uuid, created_at, name) " + "VALUES (:uuid, :createdAt, :name)")
  void insert(@BindBean OwnerRow row);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM owners WHERE name = :name)")
  boolean exists(String name);

  @SqlQuery("SELECT * FROM owners WHERE uuid = :rowUuid")
  Optional<OwnerRow> findBy(UUID rowUuid);

  @SqlQuery("SELECT * FROM owners WHERE name = :name")
  Optional<OwnerRow> findBy(String name);

  @SqlQuery("SELECT COUNT(*) FROM owners")
  int count();
}
