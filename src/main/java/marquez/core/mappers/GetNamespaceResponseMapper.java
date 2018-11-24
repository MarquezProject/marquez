package marquez.core.mappers;

import javax.validation.constraints.NotNull;
import marquez.api.GetNamespaceResponse;
import marquez.core.models.Namespace;

// TODO: Move to marquez.api.mappers pgk
public class GetNamespaceResponseMapper extends Mapper<Namespace, GetNamespaceResponse> {
  // TODO: Remove use of CoreNamespaceToApiNamespaceMapper
  private final CoreNamespaceToApiNamespaceMapper namespaceMapper =
      new CoreNamespaceToApiNamespaceMapper();

  // TODO: GetNamespaceResponseMapper.map() should accept marquez.api.models.Namespace instead
  public GetNamespaceResponse map(@NotNull Namespace namespace) {
    return new GetNamespaceResponse(namespaceMapper.map(namespace));
  }
}
