package marquez.searchengine;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.jdbi.v3.core.Jdbi;

import marquez.searchengine.db.DatabaseConnection;
import marquez.searchengine.health.SearchHealthCheck;
import marquez.searchengine.resources.SearchResource;

@Slf4j
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
    log.info("Application starting...");
    Jdbi jdbi = DatabaseConnection.initializeJdbi();
    final SearchResource searchResource = new SearchResource(jdbi);
    environment.jersey().register(searchResource);
    environment.healthChecks().register("search-health-check", new SearchHealthCheck());
  }
}
