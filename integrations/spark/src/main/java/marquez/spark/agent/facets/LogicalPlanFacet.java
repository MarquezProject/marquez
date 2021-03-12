package marquez.spark.agent.facets;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.net.URI;
import lombok.Builder;
import marquez.spark.agent.client.LineageEvent.BaseFacet;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;

public class LogicalPlanFacet extends BaseFacet {
  private final LogicalPlan plan;

  @Builder
  public LogicalPlanFacet(LogicalPlan plan) {
    super(
        URI.create("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client"),
        URI.create(
            "https://github.com/MarquezProject/marquez/blob/main/experimental/integrations/"
                + "marquez-spark-agent/facets/spark-2.4/v1/logicalPlanFacet"));
    this.plan = plan;
  }

  @JsonRawValue
  public String getPlan() {
    return plan.toJSON();
  }
}
