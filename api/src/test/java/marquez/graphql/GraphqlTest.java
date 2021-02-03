package marquez.graphql;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.dropwizard.util.Resources;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.JdbiRuleInit;
import marquez.common.Utils;
import marquez.db.OpenLineageDao;
import marquez.service.OpenLineageService;
import marquez.service.RunService;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({DataAccessTests.class, IntegrationTests.class})
public class GraphqlTest {
  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();
  private static GraphQL graphQL;

  @BeforeClass
  public static void setup() throws IOException, ExecutionException, InterruptedException {
    Jdbi jdbi = dbRule.getJdbi();
    GraphqlSchemaBuilder schemaBuilder = new GraphqlSchemaBuilder(jdbi);
    graphQL = GraphQL.newGraphQL(schemaBuilder.buildSchema()).build();
    OpenLineageDao openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    LineageEvent lineageEvent =
        Utils.newObjectMapper()
            .readValue(Resources.getResource("open_lineage/event_simple.json"), LineageEvent.class);

    OpenLineageService service =
        new OpenLineageService(
            openLineageDao, mock(RunService.class), openLineageDao.createDatasetVersionDao());
    service.createAsync(lineageEvent).get();
  }

  @Test
  public void testGraphql() {
    ExecutionResult result =
        graphQL.execute(
            "{"
                + "  job(namespace: \"my-scheduler-namespace\", name: \"myjob.mytask\") {"
                + "     name"
                + "  }"
                + "}");

    assertTrue(result.getErrors().isEmpty());
    Map<String, Object> map = result.getData();
    Map<String, Object> job = (Map<String, Object>) map.get("job");

    assertEquals("myjob.mytask", job.get("name"));
  }
}
