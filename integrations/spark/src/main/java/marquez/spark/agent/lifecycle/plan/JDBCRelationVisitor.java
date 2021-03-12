package marquez.spark.agent.lifecycle.plan;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import marquez.spark.agent.client.LineageEvent.Dataset;
import marquez.spark.agent.client.LineageEvent.DatasetFacet;
import marquez.spark.agent.client.LineageEvent.DatasourceDatasetFacet;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.datasources.LogicalRelation;
import org.apache.spark.sql.execution.datasources.SaveIntoDataSourceCommand;
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions;
import org.apache.spark.sql.execution.datasources.jdbc.JDBCRelation;
import org.apache.spark.sql.execution.datasources.jdbc.JdbcRelationProvider;
import scala.runtime.AbstractFunction0;
import scala.runtime.AbstractPartialFunction;

/**
 * {@link LogicalPlan} visitor that extracts a {@link Dataset} from a {@link JDBCRelation} or a
 * {@link SaveIntoDataSourceCommand} that writes using a {@link JdbcRelationProvider}. {@link
 * Dataset} naming expects the namespace to be the JDBC connection URL (schema and authority only)
 * and the table name to be the <code>&lt;database&gt;</code>.<code>&lt;tableName&gt;</code>.
 *
 * <p>TODO If a user specifies the {@link JDBCOptions#JDBC_QUERY_STRING()} option, we do not parse
 * the sql to determine the specific tables used. Since we return a List of {@link Dataset}s, we can
 * parse the sql and determine each table referenced to return a complete list of datasets
 * referenced.
 */
public class JDBCRelationVisitor extends AbstractPartialFunction<LogicalPlan, List<Dataset>> {
  private final SQLContext sqlContext;

  public JDBCRelationVisitor(SQLContext sqlContext) {
    this.sqlContext = sqlContext;
  }

  @Override
  public boolean isDefinedAt(LogicalPlan x) {
    return jdbcRelationSupplier(x).isPresent();
  }

  private Optional<Supplier<JDBCRelation>> jdbcRelationSupplier(LogicalPlan plan) {
    if (plan instanceof SaveIntoDataSourceCommand) {
      SaveIntoDataSourceCommand command = (SaveIntoDataSourceCommand) plan;
      if (command.dataSource() instanceof JdbcRelationProvider) {
        return Optional.of(
            () ->
                (JDBCRelation)
                    ((JdbcRelationProvider) (command).dataSource())
                        .createRelation(sqlContext, command.options()));
      }
    } else if (plan instanceof LogicalRelation
        && ((LogicalRelation) plan).relation() instanceof JDBCRelation) {
      return Optional.of(() -> (JDBCRelation) ((LogicalRelation) plan).relation());
    }
    return Optional.empty();
  }

  @Override
  public List<Dataset> apply(LogicalPlan x) {
    return jdbcRelationSupplier(x)
        .map(
            s -> {
              JDBCRelation relation = s.get();
              // TODO- if a relation is composed of a complex sql query, we should attempt to
              // extract the
              // table names so that we can construct a true lineage
              String tableName =
                  relation
                      .jdbcOptions()
                      .parameters()
                      .get(JDBCOptions.JDBC_TABLE_NAME())
                      .getOrElse(
                          new AbstractFunction0<String>() {
                            @Override
                            public String apply() {
                              return "COMPLEX";
                            }
                          });
              URI connectionUri = URI.create(relation.jdbcOptions().url());
              String namespace =
                  String.format("%s://%s", connectionUri.getScheme(), connectionUri.getHost());
              String name = String.format("%s.%s", connectionUri.getPath(), tableName);
              return Collections.singletonList(
                  Dataset.builder()
                      .namespace(namespace)
                      .name(name)
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
                                      .uri(namespace)
                                      .build())
                              .build())
                      .build());
            })
        .orElse(Collections.emptyList());
  }
}
