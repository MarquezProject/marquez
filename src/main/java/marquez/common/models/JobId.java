package marquez.common.models;

import lombok.NonNull;
import lombok.Value;

/** ID for {@code Job}. */
@Value
public class JobId {
  @NonNull NamespaceName namespace;
  @NonNull JobName name;
}
