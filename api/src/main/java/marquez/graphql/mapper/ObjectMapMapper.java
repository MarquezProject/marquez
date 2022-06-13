/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.graphql.mapper;

import com.google.common.base.CaseFormat;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@Slf4j
public class ObjectMapMapper implements RowMapper<RowMap<String, Object>> {
  @Override
  public RowMap<String, Object> map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return getData(results);
  }

  protected RowMap<String, Object> getData(ResultSet results) {
    try {
      ResultSetMetaData metaData = results.getMetaData();
      RowMap<String, Object> columns = new RowMap<>();
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        columns.put(
            CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, metaData.getColumnName(i)),
            results.getObject(i));
      }
      return columns;
    } catch (SQLException e) {
      log.error("Unable to get column names", e);
    }
    return new RowMap<>();
  }
}
