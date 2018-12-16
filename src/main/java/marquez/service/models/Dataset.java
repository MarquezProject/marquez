package marquez.service.models;

import java.time.Instant;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class Dataset {
  @Getter @NonNull private final DatasetUrn datasetUrn;
  @Getter @NonNull private final Instant createdAt;
  private final Description description;

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }
}
