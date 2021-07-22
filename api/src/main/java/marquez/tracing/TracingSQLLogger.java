package marquez.tracing;

import com.codahale.metrics.jdbi3.strategies.SmartNameStrategy;
import com.google.common.base.Stopwatch;
import io.sentry.ISpan;
import io.sentry.Sentry;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TracingSQLLogger implements SqlLogger {
  private final SqlLogger delegate;
  private static final SmartNameStrategy naming = new SmartNameStrategy();

  public TracingSQLLogger(SqlLogger delegate) {
    super();
    this.delegate = delegate;
  }

  private final ThreadLocal<Stopwatch> sw = ThreadLocal.withInitial(Stopwatch::createUnstarted);

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
    sw.get().start();
  }

  public void logAfterExecution(StatementContext context) {
    Stopwatch stopwatch = sw.get();
    stopwatch.stop();
    Logger logger = LoggerFactory.getLogger(TracingSQLLogger.class);
    logger.info(
        "Elapsed {} millis executing sql {}", stopwatch.elapsed().toMillis(), context.getRenderedSql());
    ISpan span = Sentry.getSpan();
    if (span != null) {
      span.finish();
    }
    delegate.logAfterExecution(context);
    sw.remove();
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
