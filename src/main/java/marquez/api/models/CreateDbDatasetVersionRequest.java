package marquez.api.models;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public final class CreateDbDatasetVersionRequest extends CreateDatasetVersionRequest {
  @Getter @NonNull private final String connectionUrl;
  @Getter @NonNull private final String schema;
  @Getter @NonNull private final String table;
  private final String description;

  public CreateDbDatasetVersionRequest(
      final DatasetType type,
      final String connectionUrl,
      final String schema,
      final String table,
      final String description) {
    super(type);
    this.connectionUrl = requireNonNull(connectionUrl);
    this.schema = requireNonNull(schema);
    this.table = requireNonNull(table);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
