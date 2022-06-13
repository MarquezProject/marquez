/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.Columns;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class NamespaceMapper implements RowMapper<Namespace> {
  @Override
  public Namespace map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new Namespace(
        NamespaceName.of(stringOrThrow(results, Columns.NAME)),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        OwnerName.of(stringOrThrow(results, Columns.CURRENT_OWNER_NAME)),
        stringOrNull(results, Columns.DESCRIPTION));
  }
}
