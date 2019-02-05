package marquez.api.mappers;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import marquez.api.models.NamespaceResponse;
import marquez.service.models.Namespace;

public final class NamespaceResponseMapper {
  private NamespaceResponseMapper() {}

  public static NamespaceResponse map(@NonNull Namespace namespace) {
    return new NamespaceResponse(
        namespace.getName(),
        namespace.getCreatedAt().toString(),
        namespace.getOwnerName(),
        namespace.getDescription());
  }

  public static List<NamespaceResponse> map(@NonNull List<Namespace> namespaces) {
    return namespaces.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(
            namespaces.stream().map(namespace -> map(namespace)).collect(toList()));
  }
}
