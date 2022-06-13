/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringArrayOrThrow;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;

import com.google.common.collect.ImmutableSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.NonNull;
import marquez.common.models.Field;
import marquez.common.models.FieldName;
import marquez.common.models.TagName;
import marquez.db.Columns;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetFieldMapper implements RowMapper<Field> {
  @Override
  public Field map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new Field(
        FieldName.of(stringOrThrow(results, Columns.NAME)),
        stringOrNull(results, Columns.TYPE),
        toTagNames(stringArrayOrThrow(results, Columns.TAGS)),
        stringOrNull(results, Columns.DESCRIPTION));
  }

  private ImmutableSet<TagName> toTagNames(List<String> tags) {
    if (tags == null) {
      return ImmutableSet.of();
    }
    return tags.stream().map(TagName::of).collect(ImmutableSet.toImmutableSet());
  }
}
