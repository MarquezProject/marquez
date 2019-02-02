package marquez.core.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.models.CreateNamespaceRequest;
import marquez.api.models.NamespaceResponse;
import marquez.core.models.Namespace;

public class NamespaceApiMapper extends Mapper<NamespaceResponse, Namespace> {
  public Namespace map(NamespaceResponse namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new Namespace(
        null, namespace.getName().toLowerCase(), namespace.getOwner(), namespace.getDescription());
  }

  public Namespace fromString(String namespaceName, CreateNamespaceRequest request) {
    return map(
        new NamespaceResponse(namespaceName, null, request.getOwner(), request.getDescription()));
  }
}
