package marquez.common.models;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
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

  public static DatasourceUrn from(
      @NonNull ConnectionUrl connectionUrl, @NonNull DatasourceName name) {
    return from(connectionUrl.getDatasourceType().toString(), name.getValue());
  }

  public static DatasourceUrn fromString(@NonNull final String value) {
    return new DatasourceUrn(value);
  }

  public DatasourceType getDatasourceType() {
    final String datasourceType = value.split(":")[2];
    return DatasourceType.fromString(datasourceType);
  }

  public DatasourceName getDatasourceName() {
    final String datasourceName = value.split(":")[3];
    return DatasourceName.fromString(datasourceName);
  }
}
