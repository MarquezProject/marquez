package marquez.searchengine;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.IOException;
import marquez.searchengine.health.SearchHealthCheck;
import marquez.searchengine.resources.SearchResource;

public class SearchApplication extends Application<SearchConfig> {

  public static void main(String[] args) throws Exception {
    new SearchApplication().run(args);
  }

  @Override
  public String getName() {
    return "search-service";
  }

  @Override
  public void initialize(Bootstrap<SearchConfig> bootstrap) {}

  @Override
  public void run(SearchConfig configuration, Environment environment) throws IOException {
    final SearchResource searchResource = new SearchResource();
    environment.jersey().register(searchResource);
    environment.healthChecks().register("search-health-check", new SearchHealthCheck());
  }
}
