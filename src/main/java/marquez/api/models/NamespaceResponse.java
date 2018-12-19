package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamespaceResponse {

  @JsonProperty("name")
  String name;

  @JsonProperty("createdAt")
  String createdAt;

  @JsonProperty("owner")
  String owner;

  @JsonProperty("description")
  String description;
}
