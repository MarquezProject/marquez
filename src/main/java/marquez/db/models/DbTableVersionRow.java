package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public final class DbTableVersionRow {
  @NonNull private final UUID uuid;
  private final Instant createdAt;
  @NonNull private final UUID datasetUuid;
  @NonNull private final UUID dbTableInfoUuid;
  @NonNull private final String dbTable;
}
