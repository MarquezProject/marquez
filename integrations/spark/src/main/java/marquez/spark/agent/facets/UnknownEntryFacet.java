package marquez.spark.agent.facets;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.EqualsAndHashCode;
import lombok.Value;
import marquez.spark.agent.client.LineageEvent;

@Value
@EqualsAndHashCode(callSuper = true)
public class UnknownEntryFacet extends LineageEvent.BaseFacet {

  FacetEntry output;
  List<FacetEntry> inputs;

  @Value
  public static class AttributeField {
    String name;
    String type;
    Map<String, Object> metadata;
  }

  @Value
  public static class FacetEntry {
    @JsonRawValue
    String description;
    List<AttributeField> inputAttributes;
    List<AttributeField> outputAttributes;
  }
}
