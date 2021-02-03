package marquez.graphql;

import graphql.kickstart.execution.context.DefaultGraphQLContext;
import graphql.kickstart.execution.context.GraphQLContext;
import graphql.kickstart.servlet.context.DefaultGraphQLWebSocketContext;
import graphql.kickstart.servlet.context.GraphQLServletContextBuilder;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

public class CustomGraphQLContextBuilder implements GraphQLServletContextBuilder {

  public CustomGraphQLContextBuilder() {}

  @Override
  public GraphQLContext build(HttpServletRequest req, HttpServletResponse response) {
    return MarquezGraphqlContext.createServletContext(buildDataLoaderRegistry(), null)
        .with(req)
        .with(response)
        .build();
  }

  @Override
  public GraphQLContext build() {
    return new DefaultGraphQLContext(buildDataLoaderRegistry(), null);
  }

  @Override
  public GraphQLContext build(Session session, HandshakeRequest request) {
    return DefaultGraphQLWebSocketContext.createWebSocketContext(buildDataLoaderRegistry(), null)
        .with(session)
        .with(request)
        .build();
  }

  private DataLoaderRegistry buildDataLoaderRegistry() {
    DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
    dataLoaderRegistry.register(
        "datasetLineage",
        new DataLoader<Integer, String>(
            customerIds ->
                CompletableFuture.supplyAsync(
                    () -> {
                      return null;
                    })));
    return dataLoaderRegistry;
  }
}
