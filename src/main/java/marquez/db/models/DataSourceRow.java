package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSource;

@Data
@Builder
public final class DataSourceRow {
  @NonNull private final UUID uuid;
  @NonNull private final Instant createdAt;
  @NonNull private final DataSource dataSource;
  @NonNull private final ConnectionUrl connectionUrl;
}
