package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ListNamespacesResponse {
  private final List<marquez.api.Namespace> namespaces;

  public ListNamespacesResponse(@JsonProperty("namespaces") List<Namespace> namespaces) {
    this.namespaces = namespaces;
  }

  @JsonProperty("namespaces")
  public List<marquez.api.Namespace> getNamespaces() {
    return this.namespaces;
  }
}
