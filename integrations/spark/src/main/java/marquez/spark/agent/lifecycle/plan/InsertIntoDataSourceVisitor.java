package marquez.spark.agent.lifecycle.plan;

import java.util.List;
import marquez.spark.agent.client.LineageEvent.Dataset;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.datasources.InsertIntoDataSourceCommand;
import scala.PartialFunction;
import scala.runtime.AbstractPartialFunction;

/**
 * {@link LogicalPlan} visitor that matches an {@link InsertIntoDataSourceCommand} and extracts the
 * output {@link Dataset} being written.
 */
public class InsertIntoDataSourceVisitor
    extends AbstractPartialFunction<LogicalPlan, List<Dataset>> {
  private final List<PartialFunction<LogicalPlan, List<Dataset>>> datasetProviders;

  public InsertIntoDataSourceVisitor(
      List<PartialFunction<LogicalPlan, List<Dataset>>> datasetProviders) {
    this.datasetProviders = datasetProviders;
  }

  @Override
  public boolean isDefinedAt(LogicalPlan x) {
    return x instanceof InsertIntoDataSourceCommand;
  }

  @Override
  public List<Dataset> apply(LogicalPlan x) {
    return PlanUtils.applyFirst(
        datasetProviders, ((InsertIntoDataSourceCommand) x).logicalRelation());
  }
}
