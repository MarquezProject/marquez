/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.tracing;

import com.codahale.metrics.jdbi3.strategies.SmartNameStrategy;
import io.sentry.ISpan;
import io.sentry.Sentry;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;

public class TracingSQLLogger implements SqlLogger {
  private final SqlLogger delegate;
  private static final SmartNameStrategy naming = new SmartNameStrategy();

  public TracingSQLLogger(SqlLogger delegate) {
    super();
    this.delegate = delegate;
  }

  private String taskName(StatementContext context) {
    return naming.getStatementName(context);
  }

  public void logBeforeExecution(StatementContext context) {
    ISpan span = Sentry.getSpan();
    if (span != null) {
      String taskName = taskName(context);
      String description =
          "Executed SQL: "
              + context.getParsedSql().getSql()
              + "\nJDBI SQL: "
              + context.getRenderedSql()
              + "\nBinding: "
              + context.getBinding();
      span = span.startChild(taskName, description);
    }
    delegate.logBeforeExecution(context);
  }

  public void logAfterExecution(StatementContext context) {
    ISpan span = Sentry.getSpan();
    if (span != null) {
      span.finish();
    }
    delegate.logAfterExecution(context);
  }

  public void logException(StatementContext context, SQLException ex) {
    Sentry.captureException(ex);
    ISpan span = Sentry.getSpan();
    if (span != null) {
      span.finish();
    }
    delegate.logException(context, ex);
  }
}
