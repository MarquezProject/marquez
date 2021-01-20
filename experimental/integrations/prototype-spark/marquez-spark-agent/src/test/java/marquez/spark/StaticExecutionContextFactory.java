package marquez.spark;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import marquez.spark.agent.MarquezContext;
import marquez.spark.agent.lifecycle.ContextFactory;
import marquez.spark.agent.lifecycle.RddExecutionContext;
import marquez.spark.agent.lifecycle.SparkSQLExecutionContext;

/** Returns deterministic fields for contexts */
public class StaticExecutionContextFactory extends ContextFactory {
  public StaticExecutionContextFactory(MarquezContext marquezContext) {
    super(marquezContext);
  }

  @Override
  public RddExecutionContext createRddExecutionContext(int jobId) {
    return new RddExecutionContext(jobId, marquezContext) {
      @Override
      public ZonedDateTime toZonedTime(long time) {
        return ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      }
    };
  }

  @Override
  public SparkSQLExecutionContext createSparkSQLExecutionContext(long executionId) {
    return new SparkSQLExecutionContext(executionId, marquezContext) {
      @Override
      public ZonedDateTime toZonedTime(long time) {
        return ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      }
    };
  }
}
