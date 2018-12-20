package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class NamespacesResponse {
  @JsonProperty("namespaces")
  @Getter
  private List<NamespaceResponse> namespaces;
}
