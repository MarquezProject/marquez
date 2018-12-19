package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.models.GetNamespaceResponse;
import marquez.core.models.Namespace;

// TODO: Move to marquez.api.mappers pgk
public class GetNamespaceResponseMapper extends Mapper<Namespace, GetNamespaceResponse> {
  // TODO: Remove use of CoreNamespaceToApiNamespaceMapper
  private final CoreNamespaceToApiNamespaceMapper namespaceMapper =
      new CoreNamespaceToApiNamespaceMapper();

  // TODO: GetNamespaceResponseMapper.map() should accept marquez.api.models.Namespace instead
  public GetNamespaceResponse map(Namespace namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new GetNamespaceResponse(namespaceMapper.map(namespace));
  }
}
