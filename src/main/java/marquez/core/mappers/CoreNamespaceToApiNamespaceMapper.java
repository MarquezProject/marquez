package marquez.core.mappers;

import java.util.Optional;

public class CoreNamespaceToApiNamespaceMapper
    implements Mapper<marquez.core.models.Namespace, marquez.api.Namespace> {
  @Override
  public Optional<marquez.api.Namespace> map(marquez.core.models.Namespace value) {
    if (value == null) {
      return Optional.empty();
    }

    marquez.api.Namespace toValue =
        new marquez.api.Namespace(
            value.getName(), value.getCreatedAt(), value.getOwnerName(), value.getDescription());
    return Optional.of(toValue);
  }
}
