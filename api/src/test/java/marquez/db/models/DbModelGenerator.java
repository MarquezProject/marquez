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
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newJobType;
import static marquez.common.models.CommonModelGenerator.newLifecycleState;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.CommonModelGenerator.newPhysicalDatasetName;
import static marquez.common.models.CommonModelGenerator.newRunId;
import static marquez.common.models.CommonModelGenerator.newSourceName;
import static marquez.common.models.CommonModelGenerator.newVersion;
import static marquez.service.models.ServiceModelGenerator.newRunArgs;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.NonNull;
import marquez.Generator;
import marquez.common.Utils;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import org.jetbrains.annotations.NotNull;

/** Generates new instances for {@code marquez.db.models} with random values used for testing. */
public final class DbModelGenerator extends Generator {
  private DbModelGenerator() {}

  private static final Instant NOW = Instant.now();

  /** Returns new {@link NamespaceRow} objects with a specified {@code limit}. */
  public static Set<NamespaceRow> newNamespaceRows(final int limit) {
    return Stream.generate(DbModelGenerator::newNamespaceRow)
        .limit(limit)
        .collect(toImmutableSet());
  }

  /** Returns a new {@link NamespaceRow} object with defaults. */
  public static NamespaceRow newNamespaceRow() {
    return new NamespaceRow(
        newRowUuid(),
        NOW,
        NOW,
        newNamespaceName().getValue(),
        newDescription(),
        newOwnerName().getValue(),
        false);
  }

  /** Returns a new {@link SourceRow} object with defaults. */
  public static SourceRow newSourceRow() {
    return new SourceRow(
        newRowUuid(),
        newDbSourceType().getValue(),
        NOW,
        NOW,
        newSourceName().getValue(),
        newConnectionUrl().toASCIIString(),
        newDescription());
  }

  public static DatasetRow newDatasetRow() {
    final NamespaceRow namespaceRow = newNamespaceRow();
    final SourceRow sourceRow = newSourceRow();
    return new DatasetRow(
        newRowUuid(),
        newDatasetType().name(),
        NOW,
        NOW,
        namespaceRow.getUuid(),
        namespaceRow.getName(),
        sourceRow.getUuid(),
        sourceRow.getName(),
        newDatasetName().getValue(),
        newPhysicalDatasetName().getValue(),
        null,
        newDescription(),
        null,
        false);
  }

  public static Set<DatasetRow> newDatasetRowsWith(
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName,
      @NonNull final UUID sourceUuid,
      @NonNull final String sourceName,
      final int limit) {
    return Stream.generate(
            () -> newDatasetRowWith(NOW, namespaceUuid, namespaceName, sourceUuid, sourceName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static Set<DatasetRow> newDatasetRowsWith(
      @NotNull final Instant now,
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName,
      @NonNull final UUID sourceUuid,
      @NonNull final String sourceName,
      final int limit) {
    return Stream.generate(
            () -> newDatasetRowWith(now, namespaceUuid, namespaceName, sourceUuid, sourceName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static DatasetRow newDatasetRowWith(
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName,
      @NonNull final UUID sourceUuid,
      @NonNull final String sourceName) {
    return newDatasetRowWith(NOW, namespaceUuid, namespaceName, sourceUuid, sourceName);
  }

  public static DatasetRow newDatasetRowWith(
      @NotNull final Instant now,
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName,
      @NonNull final UUID sourceUuid,
      @NonNull final String sourceName) {
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

  /** Returns new {@link DatasetVersionRow} objects with a specified {@code limit}. */
  public static Set<DatasetVersionRow> newDatasetVersionsRowWith(
      @NotNull final Instant now,
      @NonNull final UUID datasetUuid,
      @NonNull final String datasetName,
      @NonNull final String namespaceName,
      final int limit) {
    return Stream.generate(
            () -> newDatasetVersionRowWith(now, datasetUuid, datasetName, namespaceName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static DatasetVersionRow newDatasetVersionRowWith(
      @NotNull final Instant now,
      @NonNull final UUID datasetUuid,
      @NonNull final String datasetName,
      @NonNull final String namespaceName) {
    // the run row ...
    return newDatasetVersionRowWith(
        now, datasetUuid, datasetName, namespaceName, newRunId().getValue());
  }

  public static DatasetVersionRow newDatasetVersionRowWith(
      @NotNull final Instant now,
      @NonNull final UUID datasetUuid,
      @NonNull final String datasetName,
      @NonNull final String namespaceName,
      @NonNull final UUID runUuid) {
    return new DatasetVersionRow(
        newRowUuid(),
        now,
        datasetUuid,
        newVersion().getValue(),
        null,
        newLifecycleState(),
        runUuid,
        datasetName,
        namespaceName);
  }

  /** Returns new {@link JobRow} objects with a specified {@code limit}. */
  public static Set<JobRow> newJobRowsWith(
      @NotNull final Instant now,
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName,
      final int limit) {
    return Stream.generate(() -> newJobRowWith(now, namespaceUuid, namespaceName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static JobRow newJobRowWith(
      @NonNull final UUID namespaceUuid, @NonNull final String namespaceName) {
    return newJobRowWith(NOW, namespaceUuid, namespaceName);
  }

  public static JobRow newJobRowWith(
      @NotNull final Instant now,
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName) {
    final String parentJobName = newJobName().getValue();
    final UUID parentJobUuid = newRowUuid();
    final String jobName = newJobName().getValue();
    final String jobSimpleName = jobName;
    return new JobRow(
        newRowUuid(),
        newJobType().name(),
        now,
        now,
        namespaceUuid,
        namespaceName,
        jobName,
        jobSimpleName,
        parentJobName,
        parentJobUuid,
        newDescription(),
        null,
        newLocation().toString(),
        null,
        null,
        null);
  }

  /** Returns new {@link JobVersionRow} objects with a specified {@code limit}. */
  public static Set<JobVersionRow> newJobVersionRowsWith(
      @NotNull final Instant now,
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName,
      @NonNull final UUID jobUuid,
      @NonNull final String jobName,
      @NonNull final Set<DatasetRow> inputs,
      @NonNull final Set<DatasetRow> outputs,
      final int limit) {
    return Stream.generate(
            () ->
                newJobVersionRowWith(
                    now, namespaceUuid, namespaceName, jobUuid, jobName, inputs, outputs))
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static JobVersionRow newJobVersionRowWith(
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName,
      @NonNull final UUID jobUuid,
      @NonNull final String jobName,
      @NonNull final Set<DatasetRow> inputs,
      @NonNull final Set<DatasetRow> outputs) {
    return newJobVersionRowWith(
        NOW, namespaceUuid, namespaceName, jobUuid, jobName, inputs, outputs);
  }

  public static JobVersionRow newJobVersionRowWith(
      @NotNull final Instant now,
      @NonNull final UUID namespaceUuid,
      @NonNull final String namespaceName,
      @NonNull final UUID jobUuid,
      @NonNull final String jobName,
      @NonNull final Set<DatasetRow> inputs,
      @NonNull final Set<DatasetRow> outputs) {
    return new JobVersionRow(
        newRowUuid(),
        now,
        now,
        jobUuid,
        jobName,
        inputs.stream().map(DatasetRow::getUuid).collect(toImmutableList()),
        outputs.stream().map(DatasetRow::getUuid).collect(toImmutableList()),
        newLocation().toString(),
        newVersion().getValue(),
        newRunId().getValue(),
        namespaceUuid,
        namespaceName);
  }

  public static RunArgsRow newRunArgRow() {
    final Map<String, String> runArgs = newRunArgs();
    final String runArgsAsJson = Utils.toJson(newRunArgs());
    final String checksum = Utils.checksumFor(runArgs);
    return new RunArgsRow(newRowUuid(), NOW, runArgsAsJson, checksum);
  }

  /** Returns new {@link RunRow} objects with a specified {@code limit}. */
  public static Set<RunRow> newRunRowsWith(
      @NotNull final Instant now,
      @NonNull final UUID jobUuid,
      @NonNull final UUID jobVersionUuid,
      @NonNull final UUID runArgUuid,
      final int limit) {
    return Stream.generate(() -> newRunRowWith(now, jobUuid, jobVersionUuid, runArgUuid))
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static RunRow newRunRowWith(
      @NonNull final UUID jobUuid,
      @NonNull final UUID jobVersionUuid,
      @NonNull final UUID runArgUuid) {
    return newRunRowWith(NOW, jobUuid, jobVersionUuid, runArgUuid);
  }

  public static RunRow newRunRowWith(
      @NotNull final Instant now,
      @NonNull final UUID jobUuid,
      @NonNull final UUID jobVersionUuid,
      @NonNull final UUID runArgUuid) {
    return new RunRow(
        newRowUuid(),
        now,
        now,
        jobUuid,
        jobVersionUuid,
        null,
        runArgUuid,
        now,
        now,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
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
