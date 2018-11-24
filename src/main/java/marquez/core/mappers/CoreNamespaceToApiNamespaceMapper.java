package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

// TODO: Move to marquez.api.mappers pgk
// TODO: Rename class to NamespaceMapper
public class CoreNamespaceToApiNamespaceMapper
    extends Mapper<marquez.core.models.Namespace, marquez.api.Namespace> {
  public marquez.api.Namespace map(marquez.core.models.Namespace namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new marquez.api.Namespace(
        namespace.getName(),
        namespace.getCreatedAt(),
        namespace.getOwnerName(),
        namespace.getDescription());
  }
}
