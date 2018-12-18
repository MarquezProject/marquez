package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class DatasetResponse {
  @Getter
  @NonNull
  @JsonProperty("urn")
  private final String urn;

  @Getter
  @NonNull
  @JsonProperty("createdAt")
  private final String createdAt;

  @JsonProperty("description")
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
