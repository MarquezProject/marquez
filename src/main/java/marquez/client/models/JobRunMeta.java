package marquez.client.models;

import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public final class JobRunMeta {
  private final Instant nominalStartTime;
  private final Instant nominalEndTime;
  private final String runArgs;

  public JobRunMeta(
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @Nullable final String runArgs) {
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.runArgs = runArgs;
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public Optional<String> getRunArgs() {
    return Optional.ofNullable(runArgs);
  }
}
