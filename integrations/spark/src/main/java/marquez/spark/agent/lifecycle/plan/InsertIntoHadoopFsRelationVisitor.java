package marquez.spark.agent.lifecycle.plan;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import marquez.spark.agent.client.LineageEvent.Dataset;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.datasources.InsertIntoHadoopFsRelationCommand;
import scala.runtime.AbstractPartialFunction;

/**
 * {@link LogicalPlan} visitor that matches an {@link InsertIntoHadoopFsRelationCommand} and
 * extracts the output {@link Dataset} being written.
 */
public class InsertIntoHadoopFsRelationVisitor
    extends AbstractPartialFunction<LogicalPlan, List<Dataset>> {

  @Override
  public boolean isDefinedAt(LogicalPlan x) {
    return x instanceof InsertIntoHadoopFsRelationCommand;
  }

  @Override
  public List<Dataset> apply(LogicalPlan x) {
    InsertIntoHadoopFsRelationCommand command = (InsertIntoHadoopFsRelationCommand) x;
    URI outputPath = command.outputPath().toUri();
    return Collections.singletonList(PlanUtils.getDataset(outputPath, command.schema()));
  }
}
