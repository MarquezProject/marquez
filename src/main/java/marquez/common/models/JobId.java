package marquez.common.models;

import lombok.NonNull;
import lombok.Value;

/** ID for {@link Job}. */
@Value
public class JobId {
  @NonNull NamespaceName namespaceName;
  @NonNull JobName name;
}
