package marquez.core.mappers;

import java.util.Optional;

public abstract class Mapper<A, B> {
  public Optional<B> mapAsOptional(A value) {
    return Optional.ofNullable(map(value));
  }

  public Optional<B> mapIfPresent(Optional<A> value) {
    return value.map(this::map);
  }

  public abstract B map(A value);
}
