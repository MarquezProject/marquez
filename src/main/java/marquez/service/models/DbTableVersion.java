package marquez.service.models;

import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSource;
import marquez.common.models.Db;
import marquez.common.models.Description;
import marquez.common.models.Schema;
import marquez.common.models.Table;

@EqualsAndHashCode
@ToString
public final class DbTableVersion {
  @Getter private final DataSource dataSource;
  @Getter private final ConnectionUrl connectionUrl;
  @Getter private final Db db;
  @Getter private final Schema schema;
  @Getter private final Table table;
  private final Description description;

  public DbTableVersion(
      @NonNull final ConnectionUrl connectionUrl,
      @NonNull final Schema schema,
      @NonNull final Table table,
      @Nullable final Description description) {
    final URI uri = connectionUrl.toUri();

    this.dataSource = DataSource.of(uri);
    this.connectionUrl = connectionUrl;
    this.db = Db.of(uri);
    this.schema = schema;
    this.table = table;
    this.description = description;
  }

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }
}
