package marquez.common.models;

import java.net.URI;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class DataSource {
  @Getter @NonNull private final String value;

  public static DataSource of(@NonNull final URI uri) {
    return of(uri.getScheme());
  }
}
