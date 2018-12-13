package marquez.common;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;

public interface Mapper<A, B> {
  default List<B> map(@NonNull List<A> value) {
    return value.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(value.stream().map(this::map).collect(toList()));
  }

  B map(A value);
}
