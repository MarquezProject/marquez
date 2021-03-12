package marquez.spark.agent.lifecycle.plan;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import marquez.spark.agent.client.LineageEvent.Dataset;
import marquez.spark.agent.client.LineageEvent.DatasetFacet;
import marquez.spark.agent.client.LineageEvent.DatasourceDatasetFacet;
import marquez.spark.agent.client.LineageEvent.SchemaDatasetFacet;
import marquez.spark.agent.client.LineageEvent.SchemaField;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.PartialFunction;
import scala.PartialFunction$;

/**
 * Utility functions for traversing a {@link
 * org.apache.spark.sql.catalyst.plans.logical.LogicalPlan}.
 */
public class PlanUtils {

  /**
   * Merge a list of {@link PartialFunction}s and return the first value where the function is
   * defined or null if no function matches the input.
   *
   * @param fns
   * @param arg
   * @param <T>
   * @param <R>
   * @return
   */
  public static <T, R> R applyFirst(List<PartialFunction<T, R>> fns, T arg) {
    PartialFunction<T, R> fn = merge(fns);
    if (fn.isDefinedAt(arg)) {
      return fn.apply(arg);
    }
    return null;
  }

  /**
   * Given a list of {@link PartialFunction}s merge to produce a single function that will test the
   * input against each function one by one until a match is found or {@link
   * PartialFunction$#empty()} is returned.
   *
   * @param fns
   * @param <T>
   * @param <R>
   * @return
   */
  public static <T, R> PartialFunction<T, R> merge(List<PartialFunction<T, R>> fns) {
    return fns.stream().reduce((a, b) -> a.orElse(b)).orElse(PartialFunction$.MODULE$.empty());
  }

  /**
   * Given a schema, construct a valid {@link SchemaDatasetFacet}.
   *
   * @param structType
   * @return
   */
  public static SchemaDatasetFacet schemaFacet(StructType structType) {
    return SchemaDatasetFacet.builder()
        ._producer(URI.create("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client"))
        ._schemaURL(
            URI.create("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/schemaDatasetFacet"))
        .fields(transformFields(structType.fields()))
        .build();
  }

  private static List<SchemaField> transformFields(StructField[] fields) {
    List<SchemaField> list = new ArrayList<>();
    for (StructField field : fields) {
      list.add(SchemaField.builder().name(field.name()).type(field.dataType().typeName()).build());
    }
    return list;
  }

  /**
   * Given a {@link URI}, construct a valid {@link Dataset} following the expected naming
   * conventions.
   *
   * @param outputPath
   * @param schema
   * @return
   */
  public static Dataset getDataset(URI outputPath, StructType schema) {
    String namespace = String.format("%s://%s", outputPath.getScheme(), outputPath.getAuthority());
    return Dataset.builder()
        .namespace(namespace)
        .name(outputPath.getPath())
        .facets(
            DatasetFacet.builder()
                .schema(schemaFacet(schema))
                .dataSource(DatasourceDatasetFacet.builder().uri(namespace).build())
                .build())
        .build();
  }
}
