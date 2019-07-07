package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
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
public final class DatasetRequest {
  @Getter @NotNull private final String name;
  @Getter @NotNull private final String datasourceUrn;
  @Nullable private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
