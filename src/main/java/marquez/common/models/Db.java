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
public final class Db {
  @Getter @NonNull private final String value;

  public static Db of(@NonNull URI uri) {
    // TODO: uri.getPath()
    return of("");
  }
}
