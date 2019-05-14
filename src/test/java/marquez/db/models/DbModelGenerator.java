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
import marquez.common.models.Description;
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
            .uuid(newRowUuid())
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
        .uuid(newRowUuid())
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
        newRowUuid(),
        newNamespaceRow().getUuid(),
        newDatasourceRow().getUuid(),
        datasetUrn,
        newDescription(),
        false);
  }

  public static DatasetRow newDatasetRowWith(Description description) {
    return newDatasetRowWith(
        newRowUuid(),
        newNamespaceRow().getUuid(),
        newDatasourceRow().getUuid(),
        newDatasetUrn(),
        description,
        false);
  }

  public static DatasetRow newDatasetRowWith(boolean wasUpdated) {
    return newDatasetRowWith(
        newRowUuid(),
        newNamespaceRow().getUuid(),
        newDatasourceRow().getUuid(),
        newDatasetUrn(),
        newDescription(),
        wasUpdated);
  }

  public static DatasetRow newDatasetRowWith(UUID uuid, boolean wasUpdated) {
    return newDatasetRowWith(
        uuid,
        newNamespaceRow().getUuid(),
        newDatasourceRow().getUuid(),
        newDatasetUrn(),
        newDescription(),
        wasUpdated);
  }

  public static DatasetRow newDatasetRowWith(UUID namespaceUuid, UUID datasourceUuid) {
    return newDatasetRowWith(
        newRowUuid(), namespaceUuid, datasourceUuid, newDatasetUrn(), newDescription(), false);
  }

  public static DatasetRow newDatasetRowWith(
      UUID namespaceUuid, UUID datasourceUuid, DatasetUrn datasetUrn) {
    return newDatasetRowWith(
        newRowUuid(), namespaceUuid, datasourceUuid, datasetUrn, newDescription(), false);
  }

  public static DatasetRow newDatasetRowWith(UUID uuid, UUID namespaceUuid, UUID datasourceUuid) {
    return newDatasetRowWith(
        uuid, namespaceUuid, datasourceUuid, newDatasetUrn(), newDescription(), false);
  }

  public static DatasetRow newDatasetRowWith(
      UUID uuid,
      UUID namespaceUuid,
      UUID datasourceUuid,
      DatasetUrn datasetUrn,
      Description description,
      boolean wasUpdated) {
    final DatasetRow.DatasetRowBuilder builder =
        DatasetRow.builder()
            .uuid(uuid)
            .createdAt(newTimestamp())
            .namespaceUuid(namespaceUuid)
            .datasourceUuid(datasourceUuid)
            .name(newDatasetName().getValue())
            .urn(datasetUrn.getValue())
            .description(description.getValue());

    if (wasUpdated) {
      builder.updatedAt(newTimestamp());
      builder.currentVersionUuid(newRowUuid());
    }

    return builder.build();
  }

  public static List<DatasetRowExtended> newDatasetRowsExtended(int limit) {
    return Stream.generate(() -> newDatasetRowExtended()).limit(limit).collect(toList());
  }

  public static DatasetRowExtended newDatasetRowExtended() {
    return newDatasetRowExtendedWith(newDatasetUrn(), newDatasourceUrn());
  }

  public static DatasetRowExtended newDatasetRowExtendedWith(
      DatasetUrn datasetUrn, DatasourceUrn datasourceUrn) {
    return newDatasetRowExtendedWith(datasetUrn, datasourceUrn, newDescription(), false);
  }

  public static DatasetRowExtended newDatasetRowExtendedWith(Description description) {
    return newDatasetRowExtendedWith(newDatasetUrn(), newDatasourceUrn(), description, false);
  }

  public static DatasetRowExtended newDatasetRowExtendedWith(
      DatasetUrn datasetUrn,
      DatasourceUrn datasourceUrn,
      Description description,
      boolean wasUpdated) {
    final DatasetRowExtended.DatasetRowExtendedBuilder builder =
        DatasetRowExtended.builderExtended()
            .uuid(newRowUuid())
            .createdAt(newTimestamp())
            .namespaceUuid(newNamespaceRow().getUuid())
            .datasourceUuid(newDatasourceRow().getUuid())
            .name(newDatasetName().getValue())
            .urn(datasetUrn.getValue())
            .datasourceUrn(datasourceUrn.getValue())
            .description(description.getValue());

    if (wasUpdated) {
      builder.updatedAt(newTimestamp());
      builder.currentVersionUuid(newRowUuid());
    }

    return builder.build();
  }

  public static UUID newRowUuid() {
    return UUID.randomUUID();
  }

  public static Instant newTimestamp() {
    return Instant.now();
  }
}
