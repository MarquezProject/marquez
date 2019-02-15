package marquez.db.models;

import java.time.Instant;
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
public final class DataSourceRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final String name;
  @Getter @NonNull private final String connectionUrl;
  @Getter private final Instant createdAt;
}
