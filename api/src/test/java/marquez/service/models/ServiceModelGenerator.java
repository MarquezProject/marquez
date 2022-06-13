/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.time.temporal.ChronoUnit.HOURS;
import static marquez.common.models.CommonModelGenerator.newContext;
import static marquez.common.models.CommonModelGenerator.newDatasetId;
import static marquez.common.models.CommonModelGenerator.newDatasetIdsWith;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newFields;
import static marquez.common.models.CommonModelGenerator.newJobType;
import static marquez.common.models.CommonModelGenerator.newLifecycleState;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.CommonModelGenerator.newRunId;
import static marquez.common.models.CommonModelGenerator.newSchemaLocation;
import static marquez.common.models.CommonModelGenerator.newSourceName;
import static marquez.common.models.CommonModelGenerator.newTagName;
import static marquez.common.models.CommonModelGenerator.newTagNames;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.stream.IntStream;
import marquez.Generator;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;

/**
 * Generates new instances for {@code marquez.service.models} with random values used for testing.
 */
public final class ServiceModelGenerator extends Generator {
  private ServiceModelGenerator() {}

  /** Returns a new {@link NamespaceMeta} object. */
  public static NamespaceMeta newNamespaceMeta() {
    return new NamespaceMeta(newOwnerName(), newDescription());
  }

  /** Returns a new {@link DbTable} object. */
  public static DbTable newDbTable() {
    return newDbTableWith(newDatasetId());
  }

  /** Returns a new {@link DbTable} object with a specified {@link DatasetId}. */
  public static DbTable newDbTableWith(final DatasetId dbTableId) {
    final Instant now = newTimestamp();
    final DatasetName physicalName = newDatasetName();
    return new DbTable(
        dbTableId,
        dbTableId.getName(),
        physicalName,
        now,
        now,
        newSourceName(),
        newFields(4),
        newTagNames(2),
        null,
        newLifecycleState(),
        newDescription(),
        null,
        null,
        false);
  }

  /** Returns a new {@link DbTableMeta} object. */
  public static DbTableMeta newDbTableMeta() {
    return newDbTableMetaWith(newDatasetName());
  }

  /** Returns a new {@link DbTableMeta} object with a specified {@code dataset name}. */
  public static DbTableMeta newDbTableMetaWith(final String datasetName) {
    return newDbTableMetaWith(DatasetName.of(datasetName));
  }

  /** Returns a new {@link DbTableMeta} object with a specified {@link DatasetName}. */
  public static DbTableMeta newDbTableMetaWith(final DatasetName datasetName) {
    return new DbTableMeta(
        datasetName, newSourceName(), newFields(4), newTagNames(2), newDescription(), null);
  }

  /** Returns a new {@link StreamMeta} object */
  public static StreamMeta newStreamMeta() {
    final DatasetName physicalName = newDatasetName();
    return new StreamMeta(
        physicalName,
        newSourceName(),
        newSchemaLocation(),
        newFields(4),
        newTagNames(2),
        newDescription(),
        newRunId());
  }

  /** Returns a new {@link JobMeta} object with a default number of inputs and outputs. */
  public static JobMeta newJobMeta() {
    return newJobMetaWith(newNamespaceName(), 4, 2);
  }

  /** Returns a new {@link JobMeta} object with a specified {@link NamespaceName}. */
  public static JobMeta newJobMetaWith(final NamespaceName namespaceName) {
    return newJobMetaWith(namespaceName, 4, 2);
  }

  /**
   * Returns a new {@link JobMeta} object with a specified {@link NamespaceName} and number of
   * inputs and outputs.
   */
  public static JobMeta newJobMetaWith(
      final NamespaceName namespaceName, final int numOfInputs, final int numOfOutputs) {
    return new JobMeta(
        newJobType(),
        newInputsWith(namespaceName, numOfInputs),
        newOutputsWith(namespaceName, numOfOutputs),
        newLocation(),
        newContext(),
        newDescription(),
        null);
  }

  /** Returns a new {@link RunMeta} object. */
  public static RunMeta newRunMeta() {
    final Instant nominalStartTime = newTimestamp();
    final Instant nominalEndTime = nominalStartTime.plus(1, HOURS);
    return new RunMeta(nominalStartTime, nominalEndTime, newRunArgs());
  }

  /** Returns new run args for a {@link RunMeta} object with a default {@code limit}. */
  public static ImmutableMap<String, String> newRunArgs() {
    return newRunArgsWith(4);
  }

  /** Returns new run args for a {@link RunMeta} object with a specified {@code limit}. */
  public static ImmutableMap<String, String> newRunArgsWith(final int limit) {
    final ImmutableMap.Builder<String, String> runArgs = ImmutableMap.builder();
    IntStream.range(1, limit).forEach(i -> runArgs.put(runArgKey(i), runArgValue(i)));
    return runArgs.build();
  }

  /** Returns a new run arg key with a specified {@code key}. */
  private static String runArgKey(final int i) {
    return "test_key" + i + newId();
  }

  /** Returns a new run arg value with a specified {@code value}. */
  private static String runArgValue(final int i) {
    return "test_value" + i + newId();
  }

  /** Returns new {@link Tag} objects with a specified {@code limit}. */
  public static ImmutableSet<Tag> newTags(final int limit) {
    return java.util.stream.Stream.generate(ServiceModelGenerator::newTag)
        .limit(limit)
        .collect(toImmutableSet());
  }

  /** Returns a new {@link Tag} object. */
  public static Tag newTag() {
    return new Tag(newTagName(), newDescription());
  }

  /**
   * Returns new input {@link DatasetId} objects with a specified {@link NamespaceName} and {@code
   * limit}.
   */
  public static ImmutableSet<DatasetId> newInputsWith(
      final NamespaceName namespaceName, final int limit) {
    return newDatasetIdsWith(namespaceName, limit);
  }

  /**
   * Returns new output {@link DatasetId} objects with a specified {@link NamespaceName} and {@code
   * limit}.
   */
  public static ImmutableSet<DatasetId> newOutputsWith(
      final NamespaceName namespaceName, final int limit) {
    return newDatasetIdsWith(namespaceName, limit);
  }
}
