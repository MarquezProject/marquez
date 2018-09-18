package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdateJobRunRequest {

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
