package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ListNamespacesResponse {
  private final List<Namespace> namespaces;

  public ListNamespacesResponse(@JsonProperty("namespaces") List<Namespace> namespaces) {
    this.namespaces = namespaces;
  }

  @JsonProperty("namespaces")
  public List<Namespace> getNamespaces() {
    return this.namespaces;
  }
}
