package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;

public class CreateNamespaceResponse {

  private String owner;
  private String description;
  private Timestamp createdAt;

  public CreateNamespaceResponse(
      @JsonProperty("owner") String owner,
      @JsonProperty("description") String description,
      @JsonProperty("createdAt") Timestamp createdAt) {
    this.owner = owner;
    this.description = description;
    this.createdAt = createdAt;
  }

  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("createdAt")
  public Timestamp getCreatedAt() {
    return createdAt;
  }
}
