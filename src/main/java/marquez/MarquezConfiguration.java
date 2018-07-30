package marquez;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import javax.validation.constraints.NotNull;
import marquez.db.DbConfiguration;

class MarquezConfiguration extends Configuration {
  @NotNull
  @JsonProperty("database")
  private DbConfiguration database = new DbConfiguration();

  public DbConfiguration getDbConfiguration() {
    return database;
  }
}
