package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

public abstract class Mapper<A, B> {
  public Optional<B> mapIfPresent(A value) {
    return Optional.ofNullable(map(requireNonNull(value)));
  }

  public abstract B map(A value);
}
