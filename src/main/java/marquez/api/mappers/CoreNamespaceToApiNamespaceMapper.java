package marquez.api.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.models.NamespaceResponse;
import marquez.service.models.Namespace;

// TODO: Move to marquez.api.mappers pgk
// TODO: Rename class to NamespaceMapper
public class CoreNamespaceToApiNamespaceMapper extends Mapper<Namespace, NamespaceResponse> {
  public NamespaceResponse map(Namespace namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new NamespaceResponse(
        namespace.getName(),
        namespace.getCreatedAt().toString(),
        namespace.getOwnerName(),
        namespace.getDescription());
  }
}
