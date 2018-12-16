package marquez.service.models;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.Dataset;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DbSchema;
import marquez.common.models.DbTable;
import marquez.common.models.Description;
import marquez.common.models.Namespace;

@EqualsAndHashCode
@ToString
public final class DbTableVersion implements DatasetVersion {
  @Getter private final ConnectionUrl connectionUrl;
  @Getter private final DbSchema dbSchema;
  @Getter private final DbTable dbTable;
  private final Description description;

  public DbTableVersion(
      @NonNull final ConnectionUrl connectionUrl,
      @NonNull final DbSchema dbSchema,
      @NonNull final DbTable dbTable,
      @Nullable final Description description) {
    this.connectionUrl = connectionUrl;
    this.dbSchema = dbSchema;
    this.dbTable = dbTable;
    this.description = description;
  }

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }

  public String getQualifiedName() {
    return dbSchema.getValue() + '.' + dbTable.getValue();
  }

  @Override
  public DatasetUrn toDatasetUrn(Namespace namespace) {
    return DatasetUrn.of(namespace, Dataset.of(getQualifiedName()));
  }
}
