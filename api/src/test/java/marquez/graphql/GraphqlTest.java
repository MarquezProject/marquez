/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.graphql;

import static org.mockito.Mockito.mock;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.dropwizard.util.Resources;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import marquez.common.Utils;
import marquez.db.OpenLineageDao;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.OpenLineageService;
import marquez.service.RunService;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class GraphqlTest {
  private static GraphQL graphQL;

  @BeforeAll
  public static void setup(Jdbi jdbi) throws IOException, ExecutionException, InterruptedException {
    GraphqlSchemaBuilder schemaBuilder = new GraphqlSchemaBuilder(jdbi);
    graphQL = GraphQL.newGraphQL(schemaBuilder.buildSchema()).build();
    OpenLineageDao openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    LineageEvent lineageEvent =
        Utils.newObjectMapper()
            .readValue(Resources.getResource("open_lineage/event_simple.json"), LineageEvent.class);

    OpenLineageService service = new OpenLineageService(openLineageDao, mock(RunService.class));
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

    Assertions.assertTrue(result.getErrors().isEmpty());
    Map<String, Object> map = result.getData();
    Map<String, Object> job = (Map<String, Object>) map.get("job");

    Assertions.assertEquals("myjob.mytask", job.get("name"));
  }
}
