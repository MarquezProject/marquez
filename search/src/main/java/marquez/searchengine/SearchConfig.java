package marquez.searchengine;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class SearchConfig extends Configuration {

  @JsonProperty private boolean enabled = true;

  public boolean isEnabled() {
    return enabled;
  }
}
