package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class NamespacesResponse {
  private final List<NamespaceResponse> namespaces;

  public NamespacesResponse(@JsonProperty("namespaces") List<NamespaceResponse> namespaces) {
    this.namespaces = namespaces;
  }

  @JsonProperty("namespaces")
  public List<NamespaceResponse> getNamespaces() {
    return this.namespaces;
  }
}
