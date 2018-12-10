package marquez.service.models;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class DbDatasetVersion {
  @NonNull @Getter private final String connectionUrl;
  @NonNull @Getter private final String schema;
  @NonNull @Getter private final String table;
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
