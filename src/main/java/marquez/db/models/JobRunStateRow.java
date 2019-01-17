package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public final class JobRunStateRow {
  @NonNull private final UUID uuid;
  @NonNull private final Instant transitionedAt;
  @NonNull private final UUID jobRunUuid;
  @NonNull private final String runState;
}
