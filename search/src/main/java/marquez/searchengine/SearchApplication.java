package marquez.searchengine;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.IOException;
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
  public void initialize(Bootstrap<SearchConfig> bootstrap) {
    // Any bootstrap initialization goes here
  }

  @Override
  public void run(SearchConfig configuration, Environment environment) throws IOException {
    // Register resources
    final SearchResource searchResource = new SearchResource(configuration);
    environment.jersey().register(searchResource);
  }
}
