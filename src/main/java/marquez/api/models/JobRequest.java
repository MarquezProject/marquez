package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@EqualsAndHashCode
@ToString
public final class JobRequest {
  @Getter private final List<String> inputDatasetUrns;
  @Getter private final List<String> outputDatasetUrns;
  @Getter private final String location;
  @Nullable private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
