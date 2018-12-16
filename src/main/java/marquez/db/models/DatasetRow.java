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
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public final class DatasetRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  @Getter @NonNull private final UUID namespaceUuid;
  @Getter @NonNull private final UUID dataSourceUuid;
  @Getter @NonNull private final DatasetUrn urn;
  @Getter @NonNull private final UUID currentVersion;
  private final Description description;

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }
}
