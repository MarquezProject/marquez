package marquez.core.mappers;

import java.util.Optional;

interface Mapper<A, B> {
  B map(A value);

  Optional<B> mapIfPresent(A value);
}
