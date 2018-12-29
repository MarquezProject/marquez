package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Data
@Builder
public final class JobRunArgsRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final UUID jobRunUuid;
  @Getter @NonNull private final Long hash;
  @Getter @NonNull private final String args;
}
