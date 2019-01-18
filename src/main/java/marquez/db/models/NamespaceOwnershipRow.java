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
public final class NamespaceOwnershipRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant startedAt;
  @Getter @NonNull private final UUID namespaceUuid;
  @Getter @NonNull private final UUID ownerUuid;
  private final Instant endedAt;

  public Optional<Instant> getEndedAt() {
    return Optional.ofNullable(endedAt);
  }
}
