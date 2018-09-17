package marquez.api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import org.hibernate.validator.constraints.NotBlank;

public class GetJobRunDefinitionRequest {
  private final String id;

  @JsonCreator
  public GetJobRunDefinitionRequest(@NotBlank final String id) {
    this.id = id;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public boolean isValid() {
    // id can be converted into a valid UUID
    try {
      UUID.fromString(id);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
