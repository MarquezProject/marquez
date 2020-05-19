package marquez.client.models;

import lombok.NonNull;
import lombok.Value;

/** ID for {@link Job}. */
@Value
public class JobId {
  @NonNull String namespaceName;
  @NonNull String name;
}
