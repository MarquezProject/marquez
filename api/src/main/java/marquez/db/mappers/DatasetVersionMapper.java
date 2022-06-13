/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;
import static marquez.db.mappers.DatasetMapper.toFields;
import static marquez.db.mappers.DatasetMapper.toTags;
import static marquez.db.mappers.MapperUtils.toFacetsOrNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.Version;
import marquez.db.Columns;
import marquez.service.models.DatasetVersion;
import marquez.service.models.DbTableVersion;
import marquez.service.models.StreamVersion;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@Slf4j
public final class DatasetVersionMapper implements RowMapper<DatasetVersion> {
  @Override
  public DatasetVersion map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());

    DatasetType type = DatasetType.valueOf(stringOrThrow(results, Columns.TYPE));
    DatasetVersion datasetVersion;
    if (type == DatasetType.DB_TABLE) {
      datasetVersion =
          new DbTableVersion(
              new DatasetId(
                  NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
                  DatasetName.of(stringOrThrow(results, Columns.NAME))),
              DatasetName.of(stringOrThrow(results, Columns.NAME)),
              DatasetName.of(stringOrThrow(results, Columns.PHYSICAL_NAME)),
              timestampOrThrow(results, Columns.CREATED_AT),
              Version.of(uuidOrThrow(results, Columns.VERSION)),
              SourceName.of(stringOrThrow(results, Columns.SOURCE_NAME)),
              toFields(results, "fields"),
              columnNames.contains("tags") ? toTags(results, "tags") : null,
              stringOrNull(results, Columns.DESCRIPTION),
              stringOrNull(results, Columns.LIFECYCLE_STATE),
              null,
              toFacetsOrNull(results, Columns.FACETS));
    } else {
      datasetVersion =
          new StreamVersion(
              new DatasetId(
                  NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
                  DatasetName.of(stringOrThrow(results, Columns.NAME))),
              DatasetName.of(stringOrThrow(results, Columns.NAME)),
              DatasetName.of(stringOrThrow(results, Columns.PHYSICAL_NAME)),
              timestampOrThrow(results, Columns.CREATED_AT),
              Version.of(uuidOrThrow(results, Columns.VERSION)),
              SourceName.of(stringOrThrow(results, Columns.SOURCE_NAME)),
              toURL(stringOrThrow(results, Columns.SCHEMA_LOCATION)),
              toFields(results, "fields"),
              columnNames.contains("tags") ? toTags(results, "tags") : null,
              stringOrNull(results, Columns.DESCRIPTION),
              stringOrNull(results, Columns.LIFECYCLE_STATE),
              null,
              toFacetsOrNull(results, Columns.FACETS));
    }
    // The createdByRun can be brought in via join, similar to the JobMapper
    datasetVersion.setCreatedByRunUuid(uuidOrNull(results, "createdByRunUuid"));
    return datasetVersion;
  }

  private URL toURL(String value) {
    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      log.error("Could not decode url {}", value);
      return null;
    }
  }
}
