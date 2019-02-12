package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public final class DbTableInfoRow {
  @NonNull private final UUID uuid;
  private final Instant createdAt;
  @NonNull private final String db;
  @NonNull private final String dbSchema;
}
