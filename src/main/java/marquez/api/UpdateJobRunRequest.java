package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdateJobRunRequest {
  Logger LOG = LoggerFactory.getLogger(UpdateJobRunRequest.class);

  private final String currentState;

  @JsonCreator
  public UpdateJobRunRequest(@JsonProperty("state") final String state) {
    this.currentState = state;
  }

  @JsonProperty("state")
  public String getState() {
    return currentState;
  }
}
