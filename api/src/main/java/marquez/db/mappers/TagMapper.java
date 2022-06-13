/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.service.models.Tag;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class TagMapper implements RowMapper<Tag> {
  @Override
  public Tag map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new Tag(
        stringOrThrow(results, Columns.NAME), stringOrNull(results, Columns.DESCRIPTION));
  }
}
