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
import java.util.UUID;
import marquez.db.models.NamespaceOwnershipRow;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

public interface NamespaceOwnershipDao {
  @Transaction
  default void insertAndUpdateWith(NamespaceOwnershipRow row, UUID ownerUuid) {
    insert(row);
    update(row.getStartedAt(), row.getNamespaceUuid(), ownerUuid);
  }

  @SqlUpdate(
      "INSERT INTO namespace_ownerships (uuid, started_at, namespace_uuid, owner_uuid) "
          + "VALUES (:uuid, :startedAt, :namespaceUuid, :ownerUuid)")
  void insert(@BindBean NamespaceOwnershipRow row);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM namespace_ownerships WHERE uuid = :uuid)")
  boolean exists(UUID uuid);

  @SqlUpdate(
      "UPDATE namespace_ownerships "
          + "SET ended_at = :endedAt "
          + "WHERE namespace_uuid = :namespaceUuid AND owner_uuid = :ownerUuid")
  void update(Instant endedAt, UUID namespaceUuid, UUID ownerUuid);

  @SqlQuery("SELECT COUNT(*) FROM namespace_ownerships")
  int count();
}
