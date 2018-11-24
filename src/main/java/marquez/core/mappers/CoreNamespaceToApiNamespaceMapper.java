package marquez.core.mappers;

import javax.validation.constraints.NotNull;

public class CoreNamespaceToApiNamespaceMapper
    extends Mapper<marquez.core.models.Namespace, marquez.api.Namespace> {
  public marquez.api.Namespace map(@NotNull marquez.core.models.Namespace value) {
    return new marquez.api.Namespace(
        value.getName(), value.getCreatedAt(), value.getOwnerName(), value.getDescription());
  }
}
