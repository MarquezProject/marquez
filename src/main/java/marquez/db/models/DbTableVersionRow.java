package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import marquez.common.models.DbTable;

@Data
@Builder
public final class DbTableVersionRow {
  @NonNull private final UUID uuid;
  @NonNull private final Instant createdAt;
  @NonNull private final UUID datasetUuid;
  @NonNull private final DbTable dbTable;
  @NonNull private final UUID dbTableInfoUuid;
}
