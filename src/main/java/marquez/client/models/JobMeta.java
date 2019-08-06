package marquez.client.models;

import static marquez.client.Preconditions.checkNotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public final class JobMeta {
  private final List<String> inputDatasetUrns;
  private final List<String> outputDatasetUrns;
  @Getter private final String location;
  private final String description;

  public JobMeta(
      @Nullable final List<String> inputDatasetUrns,
      @Nullable final List<String> outputDatasetUrns,
      final String location,
      @Nullable final String description) {
    this.inputDatasetUrns = inputDatasetUrns;
    this.outputDatasetUrns = outputDatasetUrns;
    this.location = checkNotBlank(location);
    this.description = description;
  }

  public List<String> getInputDatasetUrns() {
    return (inputDatasetUrns == null) ? Collections.emptyList() : inputDatasetUrns;
  }

  public List<String> getOutputDatasetUrns() {
    return (outputDatasetUrns == null) ? Collections.emptyList() : outputDatasetUrns;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
