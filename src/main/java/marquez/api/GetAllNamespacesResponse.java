package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GetAllNamespacesResponse {

  List<Namespace> namespaces;

  public GetAllNamespacesResponse(@JsonProperty("namespaces") List<Namespace> namespaceList) {
    this.namespaces = namespaceList;
  }

  @JsonProperty("namespaces")
  public List<Namespace> getNamespaces() {
    return this.namespaces;
  }
}
