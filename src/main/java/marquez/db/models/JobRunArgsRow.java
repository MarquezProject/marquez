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
public final class JobRunArgsRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final UUID jobRunUuid;
  @Getter @NonNull private final Long hash;
  @Getter @NonNull private final String args;
  @Getter private final Instant createdAt;
}
