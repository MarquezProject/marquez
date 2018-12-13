package marquez.api.models;

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
  @Getter @NonNull private final String urn;
  @Getter @NonNull private final String createdAt;
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
