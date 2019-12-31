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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static marquez.common.models.ModelGenerator.newConnectionUrlFor;
import static marquez.common.models.ModelGenerator.newContext;
import static marquez.common.models.ModelGenerator.newDatasetName;
import static marquez.common.models.ModelGenerator.newDatasetType;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newOwnerName;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.common.models.ModelGenerator.newSourceType;
import static marquez.common.models.ModelGenerator.newTagName;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import marquez.Generator;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;

public final class ModelGenerator extends Generator {
  private ModelGenerator() {}

  public static List<NamespaceRow> newNamespaceRows(int limit) {
    return Stream.generate(() -> newNamespaceRow()).limit(limit).collect(toImmutableList());
  }

  public static NamespaceRow newNamespaceRow() {
    return newNamespaceRowWith(newNamespaceName());
  }

  public static NamespaceRow newNamespaceRowWith(final NamespaceName name) {
    final Instant now = newTimestamp();
    return new NamespaceRow(
        newRowUuid(), now, now, name.getValue(), newDescription(), newOwnerName().getValue());
  }

  public static List<SourceRow> newSourceRows(final int limit) {
    return Stream.generate(() -> newSourceRow()).limit(limit).collect(toImmutableList());
  }

  public static SourceRow newSourceRow() {
    return newSourceRowWith(newSourceName());
  }

  public static SourceRow newSourceRowWith(final SourceName name) {
    final Instant now = newTimestamp();
    final SourceType type = newSourceType();
    return new SourceRow(
        newRowUuid(),
        type.name(),
        now,
        now,
        name.getValue(),
        newConnectionUrlFor(type).toASCIIString(),
        newDescription());
  }

  public static List<DatasetRow> newDatasetRows(final int limit) {
    return Stream.generate(() -> newDatasetRow()).limit(limit).collect(toImmutableList());
  }

  public static DatasetRow newDatasetRow() {
    return newDatasetRowWith(
        newNamespaceRow().getUuid(),
        newSourceRow().getUuid(),
        toTagUuids(newTagRows(2)),
        newDatasetName());
  }

  public static List<DatasetRow> newDatasetRowsWith(
      UUID namespaceUuid, UUID sourceUuid, List<UUID> tagUuids, int limit) {
    return Stream.generate(() -> newDatasetRowWith(namespaceUuid, sourceUuid, tagUuids))
        .limit(limit)
        .collect(toImmutableList());
  }

  public static DatasetRow newDatasetRowWith(
      final UUID namespaceUuid, final UUID sourceUuid, final List<UUID> tagUuids) {
    return newDatasetRowWith(namespaceUuid, sourceUuid, tagUuids, newDatasetName());
  }

  public static DatasetRow newDatasetRowWith(
      final UUID namespaceUuid,
      final UUID sourceUuid,
      final List<UUID> tagUuids,
      final DatasetName name) {
    final Instant now = newTimestamp();
    final DatasetName physicalName = name;
    return new DatasetRow(
        newRowUuid(),
        newDatasetType().name(),
        now,
        now,
        namespaceUuid,
        sourceUuid,
        name.getValue(),
        physicalName.getValue(),
        tagUuids,
        null,
        newDescription(),
        null);
  }

  public static List<TagRow> newTagRows(final int limit) {
    return Stream.generate(() -> newTagRow()).limit(limit).collect(toImmutableList());
  }

  public static TagRow newTagRow() {
    return newTagRowWith(newTagName());
  }

  public static TagRow newTagRowWith(final String name) {
    final Instant now = newTimestamp();
    return new TagRow(newRowUuid(), now, now, name, newDescription());
  }

  public static List<UUID> toTagUuids(final List<TagRow> rows) {
    return rows.stream().map(row -> row.getUuid()).collect(toImmutableList());
  }

  public static List<JobContextRow> newJobContextRows(final int limit) {
    return Stream.generate(() -> newJobContextRow()).limit(limit).collect(toImmutableList());
  }

  public static JobContextRow newJobContextRow() {
    return newJobContextRowWith(newContext());
  }

  public static JobContextRow newJobContextRowWith(final Map<String, String> context) {
    return new JobContextRow(
        newRowUuid(), newTimestamp(), Utils.toJson(context), Utils.checksumFor(context));
  }

  public static UUID newRowUuid() {
    return UUID.randomUUID();
  }
}
