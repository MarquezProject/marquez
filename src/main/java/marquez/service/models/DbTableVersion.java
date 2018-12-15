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
import marquez.common.models.DbSchema;
import marquez.common.models.DbTable;
import marquez.common.models.Description;

@EqualsAndHashCode
@ToString
public final class DbTableVersion {
  @Getter private final DataSource dataSource;
  @Getter private final ConnectionUrl connectionUrl;
  @Getter private final Db db;
  @Getter private final DbSchema dbSchema;
  @Getter private final DbTable dbTable;
  private final Description description;

  public DbTableVersion(
      @NonNull final ConnectionUrl connectionUrl,
      @NonNull final DbSchema dbSchema,
      @NonNull final DbTable dbTable,
      @Nullable final Description description) {
    final URI uri = connectionUrl.toUri();

    this.dataSource = DataSource.of(uri);
    this.connectionUrl = connectionUrl;
    this.db = Db.of(uri);
    this.dbSchema = dbSchema;
    this.dbTable = dbTable;
    this.description = description;
  }

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }
}
