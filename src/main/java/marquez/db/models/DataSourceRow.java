package marquez.db.models;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public final class DataSourceRow {
  @Getter private final UUID uuid;
  @Getter private final String name;
  @Getter private final String connectionUrl;
  private final Instant createdAt;

  public DataSourceRow(
      @NonNull final UUID uuid,
      @NonNull final String name,
      @NonNull final String connectionUrl,
      @Nullable final Instant createdAt) {
    this.uuid = uuid;
    this.name = name;
    this.connectionUrl = connectionUrl;
    this.createdAt = createdAt;
  }

  public Optional<Instant> getCreatedAt() {
    return Optional.ofNullable(createdAt);
  }
}
