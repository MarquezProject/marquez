package marquez.common.models;

import lombok.NonNull;
import lombok.Value;

@Value
public final class Dataset {
  @NonNull private final String value;
}
