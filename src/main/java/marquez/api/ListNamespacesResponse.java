package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
<<<<<<< HEAD
=======
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;
>>>>>>> More test changes/fixes

public class ListNamespacesResponse {
    private final List<marquez.api.Namespace> namespaces;

    // TODO: Constructor should accept marquez.api.models.Namespace instead
    public ListNamespacesResponse(@JsonProperty("namespaces") List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

<<<<<<< HEAD
    @JsonProperty("namespaces")
    public List<marquez.api.Namespace> getNamespaces() {
        // TODO: simplify to just: return namespaces;
        return this.namespaces;
    }
}
=======
  @JsonProperty("namespaces")
  private List<marquez.core.models.Namespace> namespaces;

  @JsonProperty("namespaces")
  public List<marquez.api.Namespace> getNamespaces() {
    return namespaceMapper.map(namespaces);
  }
}
>>>>>>> More test changes/fixes
