package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public final class JobRunArgsRow {
  @NonNull private final UUID uuid;
  @NonNull private final Instant createdAt;
  @NonNull private final UUID jobRunUuid;
  @NonNull private final String runArgs;
  @NonNull private final Long checksum;
}
