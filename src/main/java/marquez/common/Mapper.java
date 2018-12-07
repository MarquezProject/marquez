package marquez.common;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

public interface Mapper<A, B> {
  default List<B> map(List<A> value) {
    requireNonNull(value);

    return value.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(value.stream().map(this::map).collect(toList()));
  }

  B map(A value);
}
