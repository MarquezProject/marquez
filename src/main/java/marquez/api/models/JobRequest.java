package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@EqualsAndHashCode
@ToString
public final class JobRequest {
  @Getter @NotNull private final List<String> inputDatasetUrns;
  @Getter @NotNull private final List<String> outputDatasetUrns;
  @Getter @NotNull private final String location;
  @Nullable private final String description;
  @Nullable private final String type;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }
}
