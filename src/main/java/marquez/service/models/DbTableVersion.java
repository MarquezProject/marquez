package marquez.service.models;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.Description;
import marquez.common.models.Schema;
import marquez.common.models.Table;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class DbTableVersion {
  @Getter @NonNull private final ConnectionUrl connectionUrl;
  @Getter @NonNull private final Schema schema;
  @Getter @NonNull private final Table table;
  private final Description description;

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }
}
