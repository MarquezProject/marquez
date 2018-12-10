package marquez.api.models;

import java.time.Instant;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import marquez.common.Urn;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class DatasetResponse {
  @NonNull @Getter private final Urn urn;
  @NonNull @Getter private final Instant createdAt;
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
