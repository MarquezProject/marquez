package marquez.client.models;

import static marquez.client.Preconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class JobRun {
  @Getter private final String runId;
  private final Instant nominalStartTime;
  private final Instant nominalEndTime;
  private final String runArgs;
  @Getter private final RunState runState;

  public JobRun(
      @JsonProperty("runId") final String runId,
      @JsonProperty("nominalStartTime") @Nullable final Instant nominalStartTime,
      @JsonProperty("nominalEndTime") @Nullable final Instant nominalEndTime,
      @JsonProperty("runArgs") @Nullable final String runArgs,
      @JsonProperty("runState") @NonNull final RunState runState) {
    this.runId = checkNotBlank(runId);
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.runArgs = runArgs;
    this.runState = runState;
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
