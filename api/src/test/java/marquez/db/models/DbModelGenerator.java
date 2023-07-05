/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static graphql.com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetType;
import static marquez.common.models.CommonModelGenerator.newDbSourceType;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.CommonModelGenerator.newPhysicalDatasetName;
import static marquez.common.models.CommonModelGenerator.newSourceName;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.NonNull;
import marquez.Generator;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import org.jetbrains.annotations.NotNull;

/** Generates new instances for {@code marquez.db.models} with random values used for testing. */
public final class DbModelGenerator extends Generator {
  private DbModelGenerator() {}

  /** Returns new {@link NamespaceRow} objects with a specified {@code limit}. */
  public static List<NamespaceRow> newNamespaceRows(int limit) {
    return Stream.generate(DbModelGenerator::newNamespaceRow)
        .limit(limit)
        .collect(toImmutableList());
  }

  /** Returns a new {@link NamespaceRow} object. */
  public static NamespaceRow newNamespaceRow() {
    final Instant now = newTimestamp();
    return new NamespaceRow(
        newRowUuid(),
        now,
        now,
        newNamespaceName().getValue(),
        newDescription(),
        newOwnerName().getValue(),
        false);
  }

  /** Returns a new {@link SourceRow} object. */
  public static SourceRow newSourceRow() {
    final Instant now = newTimestamp();
    return new SourceRow(
        newRowUuid(),
        newDbSourceType().getValue(),
        now,
        now,
        newSourceName().getValue(),
        newConnectionUrl().toASCIIString(),
        newDescription());
  }

  public static Set<DatasetRow> newDatasetRowsWith(
      @NotNull final Instant now,
      @NonNull final UUID namespaceUuid,
      @NonNull String namespaceName,
      @NonNull final UUID sourceUuid,
      @NonNull String sourceName,
      final int limit) {
    return Stream.generate(
            () -> newDatasetRowWith(now, namespaceUuid, namespaceName, sourceUuid, sourceName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static DatasetRow newDatasetRowWith(
      @NotNull final Instant now,
      @NonNull final UUID namespaceUuid,
      @NonNull String namespaceName,
      @NonNull final UUID sourceUuid,
      @NonNull String sourceName) {
    return new DatasetRow(
        newRowUuid(),
        newDatasetType().name(),
        now,
        now,
        namespaceUuid,
        namespaceName,
        sourceUuid,
        sourceName,
        newDatasetName().getValue(),
        newPhysicalDatasetName().getValue(),
        null,
        newDescription(),
        null,
        false);
  }

  /** Returns a new {@code row} uuid. */
  public static UUID newRowUuid() {
    return UUID.randomUUID();
  }

  @NotNull
  public static JobId jobIdFor(String namespaceName, String jobName) {
    return new JobId(new NamespaceName(namespaceName), new JobName(jobName));
  }
}
