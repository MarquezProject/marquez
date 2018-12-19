package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.models.NamespaceResponse;

// TODO: Move to marquez.api.mappers pgk
// TODO: Rename class to NamespaceMapper
public class CoreNamespaceToApiNamespaceMapper
    extends Mapper<marquez.core.models.Namespace, NamespaceResponse> {
  public NamespaceResponse map(marquez.core.models.Namespace namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new NamespaceResponse(
        namespace.getName(),
        namespace.getCreatedAt().toString(),
        namespace.getOwnerName(),
        namespace.getDescription());
  }
}
