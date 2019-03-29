package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;

@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@EqualsAndHashCode
@ToString
public final class DatasetRequest {
  @Getter @NotNull private final DatasetName name;
  @Getter @NotNull private final DatasourceUrn datasourceUrn;
  @Nullable private final Description description;

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }
}
