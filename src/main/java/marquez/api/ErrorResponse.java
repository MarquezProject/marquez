package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class ErrorResponse {
  @JsonProperty("error")
  private String error;
}
