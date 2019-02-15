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
public final class DatasetRow {
  @Getter private final UUID uuid;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter @NonNull private final UUID namespaceUuid;
  @Getter @NonNull private final UUID dataSourceUuid;
  @Getter @NonNull private final String urn;
  @Getter private final UUID currentVersion;
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
