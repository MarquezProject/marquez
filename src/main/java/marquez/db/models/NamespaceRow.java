package marquez.db.models;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public final class NamespaceRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final String namespace;
  @Getter @NonNull private final String currentOwnership;
  private final Instant updatedAt;
  private final String description;

  public Optional<Instant> getUpdatedAt() {
    return Optional.ofNullable(updatedAt);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
