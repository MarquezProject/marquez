package marquez.service.models;

import java.net.URI;
import java.util.Optional;
import java.util.URI;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSource;
import marquez.common.models.Database;
import marquez.common.models.Description;
import marquez.common.models.Schema;
import marquez.common.models.Table;

@EqualsAndHashCode
@ToString
public final class DbTableVersion {
  @Getter private final ConnectionUrl connectionUrl;
  @Getter private final DataSource dataSource;
  @Getter private final Database database;
  @Getter private final Schema schema;
  @Getter private final Table table;
  private final Description description;

  public DbTableVersion(
      @NonNull final ConnectionUrl connectionUrl,
      @NonNull final Schema schema,
      @NonNull final Table table) {
    this.connectionUrl = connectionUrl;

    final URI uri = URI.create(connectionUrl.getValue());
    this.dataSource = DataSource.of(uri);
    this.database = Database.of(uri);

    this.schema = schema;
    this.table = table;
  }

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }
}
