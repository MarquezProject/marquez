package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

public abstract class Mapper<A, B> {
  public Optional<B> mapAsOptional(A value) {
    return Optional.ofNullable(map(value));
  }

  public Optional<B> mapIfPresent(Optional<A> value) {
    return requireNonNull(value).map(this::map);
  }

  public abstract B map(A value);

  public abstract List<B> map(List<A> value);
}
