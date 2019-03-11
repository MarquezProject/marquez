package marquez.common.models;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = false)
public final class DatasourceUrn extends Urn {
  private static final int NUM_COMPONENTS = 2;
  private static final String URN_TYPE = "datasource";
  private static final Pattern REGEX = buildPattern(URN_TYPE, NUM_COMPONENTS);

  public DatasourceUrn(@NonNull String value) {
    super(value, REGEX);
  }

  public static DatasourceUrn from(@NonNull String type, @NonNull String name) {
    final String value = fromComponents(URN_TYPE, type, name);
    return fromString(value);
  }

  public static DatasourceUrn fromString(String value) {
    return new DatasourceUrn(value);
  }
}
