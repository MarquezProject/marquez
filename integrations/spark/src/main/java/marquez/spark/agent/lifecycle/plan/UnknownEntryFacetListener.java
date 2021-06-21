package marquez.spark.agent.lifecycle.plan;

import static java.util.Optional.ofNullable;
import static scala.collection.JavaConversions.mapAsJavaMap;
import static scala.collection.JavaConversions.seqAsJavaList;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import marquez.spark.agent.facets.UnknownEntryFacet;
import marquez.spark.agent.facets.UnknownEntryFacet.FacetEntry;
import org.apache.spark.sql.catalyst.expressions.AttributeReference;
import org.apache.spark.sql.catalyst.expressions.AttributeSet;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.types.DataType;
import scala.collection.JavaConversions;

public class UnknownEntryFacetListener implements Consumer<LogicalPlan> {

  private final Map<LogicalPlan, Object> processedNodes = new IdentityHashMap<>();

  @Override
  public void accept(LogicalPlan logicalPlan) {
    processedNodes.put(logicalPlan, null);
  }

  public UnknownEntryFacet build(LogicalPlan root) {
    FacetEntry output = mapEntry(root);
    List<FacetEntry> inputs =
        seqAsJavaList(root.collectLeaves()).stream()
            .map(this::mapEntry)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return output == null && inputs.isEmpty() ? null : new UnknownEntryFacet(output, inputs);
  }

  private FacetEntry mapEntry(LogicalPlan x) {
    if (processedNodes.containsKey(x)) return null;
    List<UnknownEntryFacet.AttributeField> output = attributeFields(x.outputSet());
    List<UnknownEntryFacet.AttributeField> input = attributeFields(x.inputSet());
    return new FacetEntry(x.toJSON(), input, output);
  }

  private List<UnknownEntryFacet.AttributeField> attributeFields(AttributeSet set) {
    return JavaConversions.<AttributeReference>asJavaCollection(set.toSet()).stream()
        .map(this::mapAttributeReference)
        .collect(Collectors.toList());
  }

  private UnknownEntryFacet.AttributeField mapAttributeReference(AttributeReference ar) {
    return new UnknownEntryFacet.AttributeField(
        ar.name(),
        ofNullable(ar.dataType()).map(DataType::typeName).orElse(null),
        new HashMap<>(mapAsJavaMap(ar.metadata().map())));
  }
}
