package marquez.api.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public final class DbTableVersionRequest extends DatasetVersionRequest {
  @Getter private final String connectionUrl;
  @Getter private final String schema;
  @Getter private final String table;

  public DbTableVersionRequest(
      @NonNull final DatasetType type,
      @NonNull final String connectionUrl,
      @NonNull final String schema,
      @NonNull final String table,
      @NonNull final String description) {
    super(type, description);
    this.connectionUrl = connectionUrl;
    this.schema = schema;
    this.table = table;
  }
}
