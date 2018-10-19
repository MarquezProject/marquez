package marquez.core.mappers;

import java.util.Optional;

interface Mapper<A, B> {
  Optional<B> map(A value);
}
