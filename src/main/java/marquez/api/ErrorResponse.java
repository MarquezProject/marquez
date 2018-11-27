package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ErrorResponse {
  private final String error;

  @JsonCreator
  public ErrorResponse(@JsonProperty("error") final String error) {
    this.error = error;
  }

  @JsonProperty("error")
  public String getError() {
    return error;
  }
}
