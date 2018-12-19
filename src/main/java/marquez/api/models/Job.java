package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class Job {
  @JsonProperty("name")
  private String name;

  @JsonProperty("createdAt")
  private Timestamp createdAt;

  @JsonProperty("inputDataSetUrns")
  private List<String> inputDataSetUrns;

  @JsonProperty("outputDataSetUrns")
  private List<String> outputDataSetUrns;

  @JsonProperty("location")
  private String location;

  @JsonProperty("description")
  private String description;
}
