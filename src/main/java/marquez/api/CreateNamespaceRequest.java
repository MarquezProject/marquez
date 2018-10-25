package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CreateNamespaceRequest {
  private final String owner;
  private final String description;

  @JsonCreator
  public CreateNamespaceRequest(
      @JsonProperty("owner") final String owner,
      @JsonProperty("description") final String description) {
    this.owner = owner;
    this.description = description;
  }

  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
}
