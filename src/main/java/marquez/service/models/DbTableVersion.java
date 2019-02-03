package marquez.service.models;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;

@EqualsAndHashCode
@ToString
@Builder
public final class DbTableVersion implements DatasetVersion {
  @Getter private final ConnectionUrl connectionUrl;
  @Getter private final DbSchemaName dbSchemaName;
  @Getter private final DbTableName dbTableName;
  private final Description description;

  public DbTableVersion(
      @NonNull final ConnectionUrl connectionUrl,
      @NonNull final DbSchemaName dbSchemaName,
      @NonNull final DbTableName dbTableName,
      @Nullable final Description description) {
    this.connectionUrl = connectionUrl;
    this.dbSchemaName = dbSchemaName;
    this.dbTableName = dbTableName;
    this.description = description;
  }

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }

  public String getQualifiedName() {
    return dbSchemaName.getValue() + '.' + dbTableName.getValue();
  }

  @Override
  public DatasetUrn toDatasetUrn(@NonNull NamespaceName namespaceName) {
    return DatasetUrn.from(namespaceName, DatasetName.fromString(getQualifiedName()));
  }
}
