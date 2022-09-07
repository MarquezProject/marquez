/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.NamespaceName;
import marquez.db.mappers.NamespaceMapper;
import marquez.db.mappers.NamespaceRowMapper;
import marquez.db.mappers.OwnerRowMapper;
import marquez.db.models.NamespaceRow;
import marquez.db.models.OwnerRow;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(NamespaceRowMapper.class)
@RegisterRowMapper(NamespaceMapper.class)
@RegisterRowMapper(OwnerRowMapper.class)
public interface NamespaceDao extends BaseDao {

  @Transaction
  default Namespace upsertNamespaceMeta(
      @NonNull NamespaceName namespaceName, @NonNull NamespaceMeta meta) {
    Instant now = Instant.now();
    NamespaceRow namespaceRow;
    if (meta.getDescription().isPresent()) {
      namespaceRow =
          upsertNamespaceRow(
              UUID.randomUUID(),
              now,
              namespaceName.getValue(),
              meta.getOwnerName().getValue(),
              meta.getDescription().get());
    } else {
      namespaceRow =
          upsertNamespaceRow(
              UUID.randomUUID(), now, namespaceName.getValue(), meta.getOwnerName().getValue());
    }
    if (namespaceRow.getCurrentOwnerName() != null
        && !namespaceRow.getCurrentOwnerName().equalsIgnoreCase(meta.getOwnerName().getValue())) {
      OwnerRow oldOwner = findOwner(namespaceRow.getCurrentOwnerName());
      OwnerRow ownerRow = upsertOwner(UUID.randomUUID(), now, meta.getOwnerName().getValue());
      if (oldOwner != null) {
        endOwnership(now, namespaceRow.getUuid(), oldOwner.getUuid());
      }
      startOwnership(UUID.randomUUID(), now, namespaceRow.getUuid(), ownerRow.getUuid());
      setCurrentOwner(namespaceRow.getUuid(), now, ownerRow.getName());
    }
    return findBy(namespaceRow.getName()).get();
  }

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM namespaces WHERE name = :name)")
  boolean exists(String name);

  @SqlUpdate(
      "UPDATE namespaces "
          + "SET updated_at = :updatedAt, "
          + "    current_owner_name = :currentOwnerName "
          + "WHERE uuid = :rowUuid")
  void setCurrentOwner(UUID rowUuid, Instant updatedAt, String currentOwnerName);

  @SqlQuery("SELECT * FROM namespaces WHERE name = :name")
  Optional<Namespace> findBy(String name);

  @SqlQuery("SELECT * FROM namespaces WHERE name = :name")
  Optional<NamespaceRow> findNamespaceByName(String name);

  @SqlQuery("SELECT * FROM namespaces ORDER BY name LIMIT :limit OFFSET :offset")
  List<Namespace> findAll(int limit, int offset);

  default NamespaceRow upsertNamespaceRow(
      UUID uuid, Instant now, String name, String currentOwnerName) {
    doUpsertNamespaceRow(uuid, now, name, currentOwnerName);
    return findNamespaceByName(name).orElseThrow();
  }

  /**
   * This query is executed by the OpenLineage write path, meaning namespaces are written to a LOT.
   * Updating the record to modify the updateAt timestamp means the same namespace is often under
   * heavy contention unnecessarily (it's really not being updated), causing some requests to wait
   * for a lock while other requests are finishing. If a single namespace is under heavy contention,
   * this can cause some requests to wait a long time - i.e., minutes. This causes unacceptable
   * latency and failures in the write path. Avoid any updates in this query to avoid unnecessary
   * locks.
   *
   * @param uuid
   * @param now
   * @param name
   * @param currentOwnerName
   */
  @SqlUpdate(
      "INSERT INTO namespaces ( "
          + "uuid, "
          + "created_at, "
          + "updated_at, "
          + "name, "
          + "current_owner_name "
          + ") VALUES ("
          + ":uuid, "
          + ":now, "
          + ":now, "
          + ":name, "
          + ":currentOwnerName) "
          + "ON CONFLICT(name) DO NOTHING")
  void doUpsertNamespaceRow(UUID uuid, Instant now, String name, String currentOwnerName);

  @SqlQuery(
      "INSERT INTO namespaces ( "
          + "uuid, "
          + "created_at, "
          + "updated_at, "
          + "name, "
          + "current_owner_name, "
          + "description "
          + ") VALUES ("
          + ":uuid, "
          + ":now, "
          + ":now, "
          + ":name, "
          + ":currentOwnerName, "
          + ":description "
          + ") ON CONFLICT(name) DO "
          + "UPDATE SET "
          + "updated_at = EXCLUDED.updated_at "
          + "RETURNING *")
  NamespaceRow upsertNamespaceRow(
      UUID uuid, Instant now, String name, String currentOwnerName, String description);

  @SqlQuery(
      "INSERT INTO owners (uuid, created_at, name) "
          + "VALUES ( "
          + ":uuid, :now, :name "
          + ") ON CONFLICT(name) DO "
          + "UPDATE SET "
          + "created_at = EXCLUDED.created_at "
          + "RETURNING *")
  OwnerRow upsertOwner(UUID uuid, Instant now, String name);

  @SqlQuery("SELECT * FROM owners WHERE name = :name")
  OwnerRow findOwner(String name);

  @SqlUpdate(
      "INSERT INTO namespace_ownerships (uuid, started_at, namespace_uuid, owner_uuid) "
          + "VALUES (:uuid, :startedAt, :namespaceUuid, :ownerUuid)")
  void startOwnership(UUID uuid, Instant startedAt, UUID namespaceUuid, UUID ownerUuid);

  @SqlUpdate(
      "UPDATE namespace_ownerships "
          + "SET ended_at = :endedAt "
          + "WHERE namespace_uuid = :namespaceUuid AND owner_uuid = :ownerUuid")
  void endOwnership(Instant endedAt, UUID namespaceUuid, UUID ownerUuid);
}
