package marquez.core.mappers;

import javax.validation.constraints.NotNull;
import marquez.api.GetNamespaceResponse;
import marquez.core.models.Namespace;

// TODO: Move to marquez.api.mappers pgk
public class GetNamespaceResponseMapper extends Mapper<Namespace, GetNamespaceResponse> {
  private final CoreNamespaceToApiNamespaceMapper namespaceMapper =
      new CoreNamespaceToApiNamespaceMapper();

  public GetNamespaceResponse map(@NotNull Namespace namespace) {
    return new GetNamespaceResponse(namespaceMapper.map(namespace));
  }
}
