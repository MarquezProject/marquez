package marquez;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

class MarquezConfiguration extends Configuration {
  @NotEmpty private String url;

  @Valid @NotNull @JsonProperty private DataSourceFactory database = new DataSourceFactory();

  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  @JsonProperty
  public String getUrl() {
    return url;
  }

  @JsonProperty
  public void setHttpURL(String Url) {
    this.url = Url;
  }
}
