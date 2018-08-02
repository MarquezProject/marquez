package marquez;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import javax.validation.constraints.NotNull;

public class MarquezConfiguration extends Configuration {
  @NotNull private DataSourceFactory database = new DataSourceFactory();
  @NotNull private FlywayFactory flyway = new FlywayFactory();

  @JsonProperty("database")
  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  @JsonProperty("database")
  public void setDataSourceFactory(final DataSourceFactory factory) {
    this.database = factory;
  }

  @JsonProperty("flyway")
  public FlywayFactory getFlywayFactory() {
    return flyway;
  }

  @JsonProperty("flyway")
  public void setFlywayFactory(final FlywayFactory factory) {
    this.flyway = factory;
  }
}
