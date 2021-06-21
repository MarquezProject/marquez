package marquez.spark.agent.facets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import marquez.spark.agent.client.LineageEvent;

@Getter
@RequiredArgsConstructor(staticName = "compose")
public class AdditionalFacetWrapper {
  final String name;
  final LineageEvent.BaseFacet facet;
}
