package marquez.common.models;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class DatasourceUrn extends Urn {
  private static final int NUM_COMPONENTS = 2;
  private static final Pattern REGEX = Urn.buildPattern(NUM_COMPONENTS);

  public static DatasourceUrn of(@NonNull String type, @NonNull String name) {
    final String value = Urn.of(type, name);
    return of(value);
  }

  public static DatasourceUrn of(String value) {
    return new DatasourceUrn(value);
  }

  public DatasourceUrn(@NonNull String value) {
    super(value, REGEX);
  }
}
