package marquez.client.models;

import static marquez.client.Preconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Job {
  @Getter private final String name;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter private final List<String> inputDatasetUrns;
  @Getter private final List<String> outputDatasetUrns;
  @Getter private final String location;
  private final String description;

  public Job(
      @JsonProperty("name") final String name,
      @JsonProperty("createdAt") @NonNull final Instant createdAt,
      @JsonProperty("updatedAt") @NonNull final Instant updatedAt,
      @JsonProperty("inputDatasetUrns") @NonNull final List<String> inputDatasetUrns,
      @JsonProperty("outputDatasetUrns") @NonNull final List<String> outputDatasetUrns,
      @JsonProperty("location") final String location,
      @JsonProperty("description") @Nullable final String description) {
    this.name = checkNotBlank(name);
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.inputDatasetUrns = inputDatasetUrns;
    this.outputDatasetUrns = outputDatasetUrns;
    this.location = checkNotBlank(location);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
