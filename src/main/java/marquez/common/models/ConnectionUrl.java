package marquez.common.models;

import lombok.NonNull;
import lombok.Value;

@Value
public final class ConnectionUrl {
  @NonNull private final String value;
}
