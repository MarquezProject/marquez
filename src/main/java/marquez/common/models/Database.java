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
public final class Database {
  @Getter @NonNull private final String value;

  public static Database of(@NonNull final URI uri) {
    return of(uri.getScheme());
  }
}
