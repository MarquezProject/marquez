package marquez.db.models;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import marquez.common.Urn;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class DatasetRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final UUID namespaceUuid;
  @Getter @NonNull private final UUID dataSourceUuid;
  @Getter @NonNull private final UUID currentVersion;
  @Getter @NonNull private final Urn urn;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
