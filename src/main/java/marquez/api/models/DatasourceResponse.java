package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasourceResponse {

  @JsonProperty("createdAt")
  private String createdAt;

  @JsonProperty("name")
  private String name;

  @JsonProperty("connectionUrl")
  private String connectionUrl;
}
