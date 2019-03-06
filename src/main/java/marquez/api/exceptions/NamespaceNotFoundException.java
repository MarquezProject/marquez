package marquez.api.exceptions;

import javax.ws.rs.NotFoundException;
import lombok.NonNull;
import marquez.common.models.NamespaceName;

public final class NamespaceNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public NamespaceNotFoundException(@NonNull final NamespaceName namespaceName) {
    super(String.format("Namespace '%s' not found.", namespaceName.getValue()));
  }
}
