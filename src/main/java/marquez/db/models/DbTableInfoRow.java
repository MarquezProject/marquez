package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import marquez.common.models.Db;
import marquez.common.models.Schema;

@Data
@Builder
public final class DbTableInfoRow {
  @NonNull private final UUID uuid;
  @NonNull private final Instant createdAt;
  @NonNull private final Db db;
  @NonNull private final Schema schema;
}
