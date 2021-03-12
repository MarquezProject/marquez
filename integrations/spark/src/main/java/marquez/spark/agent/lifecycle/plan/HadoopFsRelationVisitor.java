package marquez.spark.agent.lifecycle.plan;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import marquez.spark.agent.client.LineageEvent.Dataset;
import marquez.spark.agent.client.LineageEvent.DatasetFacet;
import marquez.spark.agent.client.LineageEvent.DatasourceDatasetFacet;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.datasources.HadoopFsRelation;
import org.apache.spark.sql.execution.datasources.LogicalRelation;
import scala.collection.JavaConversions;
import scala.runtime.AbstractPartialFunction;

/**
 * {@link LogicalPlan} visitor that extracts a {@link Dataset} from a {@link HadoopFsRelation}. It
 * is assumed that a single directory maps to a single {@link Dataset}. Any files referenced are
 * replaced by their parent directory and all files in a given directory are assumed to belong to
 * the same {@link Dataset}. Directory partitioning is currently not addressed.
 */
@Slf4j
public class HadoopFsRelationVisitor extends AbstractPartialFunction<LogicalPlan, List<Dataset>> {
  private final SparkContext context;

  public HadoopFsRelationVisitor(SparkContext context) {
    this.context = context;
  }

  @Override
  public boolean isDefinedAt(LogicalPlan x) {
    return x instanceof LogicalRelation
        && ((LogicalRelation) x).relation() instanceof HadoopFsRelation;
  }

  @Override
  public List<Dataset> apply(LogicalPlan x) {
    HadoopFsRelation relation = (HadoopFsRelation) ((LogicalRelation) x).relation();
    return JavaConversions.asJavaCollection(relation.location().rootPaths()).stream()
        .map(
            p -> {
              try {
                if (p.getFileSystem(context.hadoopConfiguration()).getFileStatus(p).isFile()) {
                  return p.getParent();
                } else {
                  return p;
                }
              } catch (IOException e) {
                log.warn("Unable to get file system for path ", e);
                return p;
              }
            })
        .distinct()
        .map(
            p -> {
              // TODO- refactor this to return a single partitioned dataset based on static
              // static partitions in the relation
              URI uri = p.toUri();
              String namespace =
                  String.format(
                      "%s://%s",
                      uri.getScheme(), Optional.ofNullable(uri.getAuthority()).orElse(""));
              return Dataset.builder()
                  .namespace(namespace)
                  .name(uri.getPath())
                  .facets(
                      DatasetFacet.builder()
                          .schema(PlanUtils.schemaFacet(relation.schema()))
                          .dataSource(
                              DatasourceDatasetFacet.builder()
                                  ._producer(
                                      URI.create(
                                          "https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client"))
                                  ._schemaURL(
                                      URI.create(
                                          "https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/spec/OpenLineage.yml#DatasourceDatasetFacet"))
                                  .uri(uri.toString())
                                  .name(namespace)
                                  .build())
                          .build())
                  .build();
            })
        .collect(Collectors.toList());
  }
}
