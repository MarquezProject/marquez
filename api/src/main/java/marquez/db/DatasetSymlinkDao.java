/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.DatasetSymlinksRowMapper;
import marquez.db.models.DatasetSymlinkRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DatasetSymlinksRowMapper.class)
public interface DatasetSymlinkDao extends BaseDao {

  default DatasetSymlinkRow upsertDatasetSymlinkRow(
      UUID uuid, String name, UUID namespaceUuid, boolean isPrimary, String type, Instant now) {
    doUpsertDatasetSymlinkRow(uuid, name, namespaceUuid, isPrimary, type, now);
    return findDatasetSymlinkByNamespaceUuidAndName(namespaceUuid, name).orElseThrow();
  }

  @SqlQuery("SELECT * FROM dataset_symlinks WHERE namespace_uuid = :namespaceUuid and name = :name")
  Optional<DatasetSymlinkRow> findDatasetSymlinkByNamespaceUuidAndName(
      UUID namespaceUuid, String name);

  @SqlUpdate(
      """
          INSERT INTO dataset_symlinks (
          dataset_uuid,
          name,
          namespace_uuid,
          is_primary,
          type,
          created_at,
          updated_at
          ) VALUES (
          :uuid,
          :name,
          :namespaceUuid,
          :isPrimary,
          :type,
          :now,
          :now)
          ON CONFLICT (name, namespace_uuid) DO NOTHING""")
  void doUpsertDatasetSymlinkRow(
      UUID uuid, String name, UUID namespaceUuid, boolean isPrimary, String type, Instant now);
}
