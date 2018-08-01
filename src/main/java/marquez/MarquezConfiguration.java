package marquez;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import javax.validation.constraints.NotNull;

public final class MarquezConfiguration extends Configuration {
  @NotNull private final DataSourceFactory database;
  @NotNull private final FlywayFactory flyway;

  @JsonCreator
  public MarquezConfiguration(
      @JsonProperty("database") final DataSourceFactory database,
      @JsonProperty("flyway") final FlywayFactory flyway) {
    this.database = database;
    this.flyway = flyway;
  }

  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  public FlywayFactory getFlywayFactory() {
    return flyway;
  }
}
