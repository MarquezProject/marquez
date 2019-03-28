package marquez.db.models;

import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;

import java.time.Instant;
import java.util.UUID;

public final class DbModelGenerator {
  private DbModelGenerator() {}

  public static NamespaceRow newNamespaceRow() {
    return newNamespaceRow(false);
  }

  public static NamespaceRow newNamespaceRow(boolean wasUpdated) {
    final NamespaceRow.NamespaceRowBuilder builder =
        NamespaceRow.builder()
            .uuid(UUID.randomUUID())
            .createdAt(newTimestamp())
            .updatedAt(newTimestamp())
            .name(newNamespaceName().getValue())
            .description(newDescription().getValue())
            .currentOwnerName(newOwnerName().getValue());

    if (wasUpdated) {
      builder.updatedAt(newTimestamp());
      builder.currentOwnerName(newOwnerName().getValue());
    }

    return builder.build();
  }

  public static DatasourceRow newDatasourceRow() {
    return DatasourceRow.builder()
        .uuid(UUID.randomUUID())
        .createdAt(newTimestamp())
        .name(newDatasourceName().getValue())
        .urn(newDatasourceUrn().getValue())
        .connectionUrl(newConnectionUrl().getRawValue())
        .build();
  }

  public static DatasetRow newDatasetRow() {
    return newDatasetRowWith(false);
  }

  public static DatasetRow newDatasetRowWith(boolean wasUpdated) {
    return newDatasetRowWith(UUID.randomUUID(), UUID.randomUUID(), wasUpdated);
  }

  public static DatasetRow newDatasetRowWith(UUID namespaceUuid, UUID datasourceUuid) {
    return newDatasetRowWith(namespaceUuid, datasourceUuid, false);
  }

  public static DatasetRow newDatasetRowWith(
      UUID namespaceUuid, UUID datasourceUuid, boolean wasUpdated) {
    final DatasetRow.DatasetRowBuilder builder =
        DatasetRow.builder()
            .uuid(UUID.randomUUID())
            .createdAt(newTimestamp())
            .namespaceUuid(namespaceUuid)
            .datasourceUuid(datasourceUuid)
            .urn(newDatasetUrn().getValue());

    if (wasUpdated) {
      builder.updatedAt(newTimestamp());
      builder.currentVersion(UUID.randomUUID());
    }

    return builder.build();
  }

  public static Instant newTimestamp() {
    return Instant.now();
  }
}
