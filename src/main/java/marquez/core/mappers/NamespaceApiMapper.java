package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.CreateNamespaceRequest;

public class NamespaceApiMapper
    extends Mapper<marquez.api.Namespace, marquez.core.models.Namespace> {
  public marquez.core.models.Namespace map(marquez.api.Namespace namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new marquez.core.models.Namespace(
        null, namespace.getName().toLowerCase(), namespace.getOwner(), namespace.getDescription());
  }

  public marquez.core.models.Namespace of(String namespaceName, CreateNamespaceRequest request) {
    return map(
        new marquez.api.Namespace(
            namespaceName, null, request.getOwner(), request.getDescription()));
  }
}
