package marquez.db.models;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
public final class JobVersionRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final UUID jobUuid;
  @Getter @NonNull private final List<String> inputDatasetUrns;
  @Getter @NonNull private final List<String> outputDatasetUrns;
  @Getter @NonNull private final UUID version;
  @Getter @NonNull private final String location;
  private final Instant updatedAt;
  private final UUID latestJobRunUuid;

  public Optional<Instant> getUpdatedAt() {
    return Optional.ofNullable(updatedAt);
  }

  public Optional<UUID> getLatestJobRunUuid() {
    return Optional.ofNullable(latestJobRunUuid);
  }
}
