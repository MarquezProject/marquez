package marquez;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;

public class MarquezConfiguration extends Configuration {
  @JsonProperty("database")
  private final DataSourceFactory database = new DataSourceFactory();

  @JsonProperty("flyway")
  private final FlywayFactory flyway = new FlywayFactory();

  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  public FlywayFactory getFlywayFactory() {
    return flyway;
  }
}
