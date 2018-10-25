package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetNamespaceResponse {

  final String name;
  final String ownerName;
  final String description;

  @JsonCreator
  public GetNamespaceResponse(
      final String name,
      @JsonProperty("owner") final String ownerName,
      @JsonProperty("description") final String description) {
    this.name = name;
    this.ownerName = ownerName;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  @JsonProperty("owner")
  public String getOwnerName() {
    return ownerName;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
}
