package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CreateJobRequest {

  @JsonProperty("location")
  @NotNull
  private String location;

  @JsonProperty("description")
  private String description;

  @JsonProperty("inputDatasetUrns")
  @NotNull
  public List<String> inputDataSetUrns;

  @JsonProperty("outputDatasetUrns")
  @NotNull
  public List<String> outputDatasetUrns;
}
