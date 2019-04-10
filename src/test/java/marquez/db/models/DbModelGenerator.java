/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.db.models;

import static java.util.stream.Collectors.toList;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.NamespaceName;

public final class DbModelGenerator {
  private DbModelGenerator() {}

  public static List<NamespaceRow> newNamespaceRows(int limit) {
    return Stream.generate(() -> newNamespaceRow()).limit(limit).collect(toList());
  }

  public static NamespaceRow newNamespaceRow() {
    return newNamespaceRowWith(newNamespaceName(), false);
  }

  public static NamespaceRow newNamespaceRowWith(NamespaceName namespaceName) {
    return newNamespaceRowWith(namespaceName, false);
  }

  public static NamespaceRow newNamespaceRowWith(NamespaceName namespaceName, boolean wasUpdated) {
    final NamespaceRow.NamespaceRowBuilder builder =
        NamespaceRow.builder()
            .uuid(UUID.randomUUID())
            .createdAt(newTimestamp())
            .updatedAt(newTimestamp())
            .name(namespaceName.getValue())
            .description(newDescription().getValue())
            .currentOwnerName(newOwnerName().getValue());

    if (wasUpdated) {
      builder.updatedAt(newTimestamp());
      builder.currentOwnerName(newOwnerName().getValue());
    }

    return builder.build();
  }

  public static List<DatasourceRow> newDatasourceRows(int limit) {
    return Stream.generate(() -> newDatasourceRow()).limit(limit).collect(toList());
  }

  public static DatasourceRow newDatasourceRow() {
    return newDatasourceRowWith(newDatasourceName(), newDatasourceUrn());
  }

  public static DatasourceRow newDatasourceRowWith(
      DatasourceName datasourceName, DatasourceUrn datasourceUrn) {
    return DatasourceRow.builder()
        .uuid(UUID.randomUUID())
        .createdAt(newTimestamp())
        .name(datasourceName.getValue())
        .urn(datasourceUrn.getValue())
        .connectionUrl(newConnectionUrl().getRawValue())
        .build();
  }

  public static List<DatasetRow> newDatasetRows(int limit) {
    return Stream.generate(() -> newDatasetRow()).limit(limit).collect(toList());
  }

  public static List<DatasetRow> newDatasetRowsWith(
      UUID namespaceUuid, UUID datasourceUuid, int limit) {
    return Stream.generate(() -> newDatasetRowWith(namespaceUuid, datasourceUuid))
        .limit(limit)
        .collect(toList());
  }

  public static DatasetRow newDatasetRow() {
    return newDatasetRowWith(false);
  }

  public static DatasetRow newDatasetRowWith(UUID uuid) {
    return newDatasetRowWith(uuid, false);
  }

  public static DatasetRow newDatasetRowWith(DatasetUrn datasetUrn) {
    return newDatasetRowWith(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), datasetUrn, false);
  }

  public static DatasetRow newDatasetRowWith(boolean wasUpdated) {
    return newDatasetRowWith(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), newDatasetUrn(), wasUpdated);
  }

  public static DatasetRow newDatasetRowWith(UUID uuid, boolean wasUpdated) {
    return newDatasetRowWith(
        uuid, UUID.randomUUID(), UUID.randomUUID(), newDatasetUrn(), wasUpdated);
  }

  public static DatasetRow newDatasetRowWith(UUID namespaceUuid, UUID datasourceUuid) {
    return newDatasetRowWith(
        UUID.randomUUID(), namespaceUuid, datasourceUuid, newDatasetUrn(), false);
  }

  public static DatasetRow newDatasetRowWith(
      UUID namespaceUuid, UUID datasourceUuid, DatasetUrn datasetUrn) {
    return newDatasetRowWith(UUID.randomUUID(), namespaceUuid, datasourceUuid, datasetUrn, false);
  }

  public static DatasetRow newDatasetRowWith(UUID uuid, UUID namespaceUuid, UUID datasourceUuid) {
    return newDatasetRowWith(uuid, namespaceUuid, datasourceUuid, newDatasetUrn(), false);
  }

  public static DatasetRow newDatasetRowWith(
      UUID uuid,
      UUID namespaceUuid,
      UUID datasourceUuid,
      DatasetUrn datasetUrn,
      boolean wasUpdated) {
    final DatasetRow.DatasetRowBuilder builder =
        DatasetRow.builder()
            .uuid(uuid)
            .createdAt(newTimestamp())
            .namespaceUuid(namespaceUuid)
            .datasourceUuid(datasourceUuid)
            .name(newDatasetName().getValue())
            .urn(datasetUrn.getValue())
            .description(newDescription().getValue());

    if (wasUpdated) {
      builder.updatedAt(newTimestamp());
      builder.currentVersionUuid(UUID.randomUUID());
    }

    return builder.build();
  }

  private static Instant newTimestamp() {
    return Instant.now();
  }
}
