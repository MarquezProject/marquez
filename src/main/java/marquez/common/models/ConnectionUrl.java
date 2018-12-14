package marquez.common.models;

import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(staticName = "of")
@EqualsAndHashCode
@ToString
public final class ConnectionUrl {
  @Getter @NonNull private final String value;

  public URI toUri() {
    return URI.create(value);
  }
}
