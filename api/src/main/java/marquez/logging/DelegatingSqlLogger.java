/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.logging;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;

/** A {@link SqlLogger} implementation that delegates to multiple {@link SqlLogger}s. */
public class DelegatingSqlLogger implements SqlLogger {
  private final List<SqlLogger> sqlLoggers;

  public DelegatingSqlLogger(SqlLogger... sqlLoggers) {
    this.sqlLoggers = Arrays.asList(sqlLoggers);
  }

  @Override
  public void logAfterExecution(StatementContext statementContext) {
    for (SqlLogger sqlLogger : sqlLoggers) {
      sqlLogger.logAfterExecution(statementContext);
    }
  }

  @Override
  public void logException(StatementContext statementContext, SQLException ex) {
    for (SqlLogger sqlLogger : sqlLoggers) {
      sqlLogger.logException(statementContext, ex);
    }
  }
}
