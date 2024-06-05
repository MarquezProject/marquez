/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.common.Utils;
import marquez.common.models.Version;
import marquez.db.mappers.DatasetSchemaVersionRowMapper;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetSchemaVersionRow;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(DatasetSchemaVersionRowMapper.class)
public interface DatasetSchemaVersionDao extends BaseDao {
  default Version upsertSchemaVersion(
      DatasetRow datasetRow, List<DatasetFieldRow> datasetFields, Instant now) {
    final Version computedVersion =
        Utils.newDatasetSchemaVersionFor(
            datasetRow.getNamespaceName(),
            datasetRow.getName(),
            datasetFields.stream()
                .map(field -> Pair.of(field.getName(), field.getType()))
                .collect(Collectors.toSet()));
    upsertSchemaVersion(computedVersion.getValue(), datasetRow.getUuid(), now)
        .ifPresent(
            newRow -> {
              // if not null it means a new insert, so we have to do the fields as well
              // if null then it means the version already exists, and so the fields must already
              // exist
              upsertFieldMappings(
                  newRow.getUuid(),
                  datasetFields.stream()
                      .map(DatasetFieldRow::getUuid)
                      .collect(Collectors.toList()));
            });
    return computedVersion;
  }

  @SqlQuery(
      "INSERT INTO dataset_schema_versions "
          + "(uuid, dataset_uuid, created_at) "
          + "VALUES (:uuid, :datasetUuid, :now) "
          + "ON CONFLICT DO NOTHING "
          + "RETURNING *")
  Optional<DatasetSchemaVersionRow> upsertSchemaVersion(UUID uuid, UUID datasetUuid, Instant now);

  @SqlBatch(
      "INSERT INTO dataset_schema_versions_field_mapping "
          + "(dataset_schema_version_uuid, dataset_field_uuid) "
          + "VALUES (:schemaVersionUuid, :fieldUuid) "
          + "ON CONFLICT DO NOTHING")
  void upsertFieldMappings(UUID schemaVersionUuid, Iterable<UUID> fieldUuid);
}
