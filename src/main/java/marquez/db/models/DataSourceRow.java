package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSource;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public final class DataSourceRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final DataSource dataSource;
  @Getter @NonNull private final ConnectionUrl connectionUrl;
}
