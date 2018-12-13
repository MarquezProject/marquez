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
public final class DbTableVersion {
  @Getter @NonNull private final String connectionUrl;
  @Getter @NonNull private final String schema;
  @Getter @NonNull private final String table;
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
