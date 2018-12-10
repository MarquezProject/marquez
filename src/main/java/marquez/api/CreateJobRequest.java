package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

// TODO: Add integration testing for this
@Data
public final class CreateJobRequest {

  @JsonProperty("location")
  private final String location;

  @JsonProperty("description")
  private final String description;

  @JsonProperty("inputDatasetUrns")
  public List<String> getInputDatasetUrns;

  @JsonProperty("oututDatasetUrns")
  public List<String> getOutputDatasetUrns;
}
