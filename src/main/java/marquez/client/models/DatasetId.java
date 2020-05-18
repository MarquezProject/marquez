package marquez.client.models;

import lombok.NonNull;
import lombok.Value;

/** ID for {@link Dataset}. */
@Value
public class DatasetId {
  @NonNull String namespaceName;
  @NonNull String name;
}
