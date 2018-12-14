package marquez.common.models;

import java.net.URI;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class Database {
  @Getter @NonNull private final String value;

  public static Database of(@NonNull final URI uri) {
    return of(uri.getScheme());
  }
}
