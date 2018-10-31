package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;
import marquez.core.models.Namespace;

public class ListNamespacesResponse {

  CoreNamespaceToApiNamespaceMapper namespaceMapper = new CoreNamespaceToApiNamespaceMapper();
  private final List<Namespace> namespaces;

  public ListNamespacesResponse(@JsonProperty("namespaces") List<Namespace> namespaceList) {
    this.namespaces = namespaceList;
  }

  @JsonProperty("namespaces")
  public List<marquez.api.Namespace> getNamespaces() {
    // TODO: simplify to just: return namespaces;
    return namespaces
        .stream()
        .map(namespaceMapper::mapAsOptional)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
