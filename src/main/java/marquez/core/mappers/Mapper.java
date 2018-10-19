package marquez.core.mappers;

interface Mapper<A, B> {
  B map(A value);
}
