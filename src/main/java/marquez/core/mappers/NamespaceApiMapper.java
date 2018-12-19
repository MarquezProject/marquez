package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.models.CreateNamespaceRequest;
import marquez.api.models.NamespaceResponse;

public class NamespaceApiMapper extends Mapper<NamespaceResponse, marquez.core.models.Namespace> {
  public marquez.core.models.Namespace map(NamespaceResponse namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new marquez.core.models.Namespace(
        null, namespace.getName().toLowerCase(), namespace.getOwner(), namespace.getDescription());
  }

  public marquez.core.models.Namespace of(String namespaceName, CreateNamespaceRequest request) {
    return map(
        new NamespaceResponse(namespaceName, null, request.getOwner(), request.getDescription()));
  }
}
