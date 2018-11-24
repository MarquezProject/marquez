package marquez.core.mappers;

import java.util.Optional;
import javax.validation.constraints.NotNull;

public abstract class Mapper<A, B> {
  public Optional<B> mapIfPresent(@NotNull A value) {
    return Optional.ofNullable(map(value));
  }

  public abstract B map(A value);
}
