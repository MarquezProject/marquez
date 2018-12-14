package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CreateJobRequest {

  @JsonProperty("location")
  private String location;

  @JsonProperty("description")
  private String description;

  @JsonProperty("inputDatasetUrns")
  public List<String> getInputDatasetUrns;

  @JsonProperty("oututDatasetUrns")
  public List<String> getOutputDatasetUrns;
}
