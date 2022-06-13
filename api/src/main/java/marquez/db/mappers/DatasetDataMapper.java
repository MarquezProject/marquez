/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.db.Columns;
import marquez.db.models.DatasetData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

@Slf4j
public class DatasetDataMapper implements RowMapper<DatasetData> {
  private static final ObjectMapper MAPPER = Utils.getMapper();

  @Override
  public DatasetData map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new DatasetData(
        uuidOrThrow(results, Columns.ROW_UUID),
        new DatasetId(
            NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
            DatasetName.of(stringOrThrow(results, Columns.NAME))),
        DatasetType.valueOf(stringOrThrow(results, Columns.TYPE)),
        DatasetName.of(stringOrThrow(results, Columns.NAME)),
        DatasetName.of(stringOrThrow(results, Columns.PHYSICAL_NAME)),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
        SourceName.of(stringOrThrow(results, Columns.SOURCE_NAME)),
        toFields(results, "fields"),
        ImmutableSet.of(),
        timestampOrNull(results, Columns.LAST_MODIFIED_AT),
        stringOrNull(results, Columns.DESCRIPTION),
        stringOrNull(results, Columns.LIFECYCLE_STATE));
  }

  public static ImmutableList<Field> toFields(ResultSet results, String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return ImmutableList.of();
    }
    PGobject pgObject = (PGobject) results.getObject(column);
    try {
      return MAPPER.readValue(pgObject.getValue(), new TypeReference<ImmutableList<Field>>() {});
    } catch (JsonProcessingException e) {
      log.error(String.format("Could not read dataset from job row %s", column), e);
      return ImmutableList.of();
    }
  }
}
