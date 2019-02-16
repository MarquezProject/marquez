package marquez.db.models;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
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
public final class DataSourceRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final String name;
  @Getter @NonNull private final String connectionUrl;
  @Nullable private final Instant createdAt;

  public Optional<Instant> getCreatedAt() {
    return Optional.ofNullable(createdAt);
  }
}
