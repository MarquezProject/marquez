package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.Objects;

public class CreateNamespaceResponse {
  final Namespace namespace;

  @JsonCreator
  public CreateNamespaceResponse(marquez.api.Namespace namespace) {
    this.namespace = namespace;
  }

  @JsonUnwrapped
  @JsonProperty("namespace")
  public Namespace getNamespace() {
    return namespace;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof GetNamespaceResponse)) return false;

    final GetNamespaceResponse other = (GetNamespaceResponse) o;
    return Objects.equals(namespace, other.namespace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace);
  }
}
