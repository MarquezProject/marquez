package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.models.Namespace;

// TODO: Move to marquez.api.mappers pgk
// TODO: Rename class to NamespaceMapper
public class CoreNamespaceToApiNamespaceMapper
    extends Mapper<marquez.core.models.Namespace, Namespace> {
  public Namespace map(marquez.core.models.Namespace namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new Namespace(
        namespace.getName(),
        namespace.getCreatedAt().toString(),
        namespace.getOwnerName(),
        namespace.getDescription());
  }
}
