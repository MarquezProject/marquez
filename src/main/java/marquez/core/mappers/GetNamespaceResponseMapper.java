package marquez.core.mappers;

import java.util.Optional;
import marquez.api.GetNamespaceResponse;
import marquez.core.models.Namespace;

public class GetNamespaceResponseMapper implements Mapper<Namespace, GetNamespaceResponse> {

  CoreNamespaceToApiNamespaceMapper namespaceMapper = new CoreNamespaceToApiNamespaceMapper();

  @Override
  public Optional<GetNamespaceResponse> map(Namespace namespace) {
    if (namespace == null) {
      return Optional.empty();
    }

    GetNamespaceResponse response = new GetNamespaceResponse(namespaceMapper.map(namespace).get());
    // TODO: make this more defensive use optional correctly
    return Optional.of(response);
  }
}
