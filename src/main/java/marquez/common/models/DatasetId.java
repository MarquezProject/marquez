package marquez.common.models;

import lombok.NonNull;
import lombok.Value;

/** ID for {@code Dataset}. */
@Value
public class DatasetId {
  @NonNull NamespaceName namespaceName;
  @NonNull DatasetName name;
}
