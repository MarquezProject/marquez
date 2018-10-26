package marquez.core.mappers;

import java.util.Optional;

interface Mapper<A, B> {
  Optional<B> to(A value);

  Optional<A> from(B value);
}
