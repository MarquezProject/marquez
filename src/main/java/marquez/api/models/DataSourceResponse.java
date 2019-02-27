package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceResponse {

  @JsonProperty("name")
  String name;

  @JsonProperty("createdAt")
  String createdAt;

  @JsonProperty("connectionUrl")
  String owner;
}
