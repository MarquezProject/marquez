package marquez.spark.agent.facets;

import java.net.URI;
import lombok.Builder;
import lombok.NonNull;
import marquez.spark.agent.client.LineageEvent.BaseFacet;

public class ErrorFacet extends BaseFacet {
  private final Exception exception;

  @Builder
  public ErrorFacet(@NonNull Exception exception) {
    super(
        URI.create("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client"),
        URI.create(
            "https://github.com/MarquezProject/marquez/blob/main/experimental/integrations/"
                + "marquez-spark-agent/facets/spark-2.4/v1/error-facet"));
    this.exception = exception;
  }

  public String getMessage() {
    return exception.getMessage();
  }
}
