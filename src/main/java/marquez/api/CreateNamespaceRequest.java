package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

public final class CreateNamespaceRequest {
  @NotEmpty private final String owner;
  @NotEmpty private final String description;

  @JsonCreator
  public CreateNamespaceRequest(
      @JsonProperty("owner") @NotNull final String owner,
      @JsonProperty("description") @NotNull final String description) {
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
