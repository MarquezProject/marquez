package marquez.api.mappers;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: Remove
public abstract class Mapper<A, B> {
  public Optional<B> mapAsOptional(A value) {
    return Optional.ofNullable(map(value));
  }

  public Optional<B> mapIfPresent(Optional<A> value) {
    return requireNonNull(value).map(this::map);
  }

  public List<B> map(List<A> value) {
    return requireNonNull(value).stream().map(this::map).collect(Collectors.toList());
  }

  public abstract B map(A value);
}
