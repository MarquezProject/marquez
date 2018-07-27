package marquez;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

class MarquezConfiguration extends Configuration {
  @Valid
  @NotNull
  @JsonProperty
  private DataSourceFactory dataSourceFactory = new DataSourceFactory();

  public DataSourceFactory getDataSourceFactory() {
    return dataSourceFactory;
  }
}
