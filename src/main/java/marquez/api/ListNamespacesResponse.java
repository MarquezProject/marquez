package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;

public class ListNamespacesResponse {
  // TODO: Remove use of CoreNamespaceToApiNamespaceMapper
  CoreNamespaceToApiNamespaceMapper namespaceMapper = new CoreNamespaceToApiNamespaceMapper();
  private final List<marquez.api.Namespace> namespaces;

  // TODO: Constructor should accept marquez.api.models.Namespace instead
  public ListNamespacesResponse(@JsonProperty("namespaces") List<Namespace> namespaces) {
    this.namespaces = namespaces;
  }

  @JsonProperty("namespaces")
  public List<marquez.api.Namespace> getNamespaces() {
    // TODO: simplify to just: return namespaces;
    return this.namespaces;
  }
}
