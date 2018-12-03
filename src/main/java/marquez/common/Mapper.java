package marquez.common;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface Mapper<A, B> {
  public default Optional<B> mapAsOptional(A value) {
    return Optional.ofNullable(map(value));
  }

  public default Optional<B> mapIfPresent(Optional<A> value) {
    return requireNonNull(value).map(this::map);
  }

  public default List<B> map(List<A> value) {
    return requireNonNull(value).stream().map(this::map).collect(Collectors.toList());
  }

  public B map(A value);
}
