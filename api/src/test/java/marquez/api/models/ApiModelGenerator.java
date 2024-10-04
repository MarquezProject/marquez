/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static graphql.com.google.common.collect.ImmutableSet.toImmutableSet;
import static io.openlineage.client.OpenLineage.RunEvent.EventType.COMPLETE;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newFieldName;
import static marquez.common.models.CommonModelGenerator.newFieldType;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newRunId;

import com.google.common.collect.ImmutableList;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NonNull;
import marquez.Generator;

/** Generates new instances for {@code marquez.api.models} with random values used for testing. */
public final class ApiModelGenerator extends Generator {
  private ApiModelGenerator() {}

  /** Returns a new {@link ActiveRun} object with a specified {@code namespace}. */
  public static ActiveRun newOlActiveRun(
      @NonNull final OpenLineage ol,
      @NonNull final OpenLineageClient olClient,
      @NonNull final String namespace) {
    return newOlActiveRunFor(ol, olClient, namespace, newJobName().getValue());
  }

  /** Returns a new {@link ActiveRun} object with a specified {@code namespace}. */
  public static ActiveRun newOlActiveRunFor(
      @NonNull final OpenLineage ol,
      @NonNull final OpenLineageClient olClient,
      @NonNull final String namespace,
      @NonNull final String jobName) {
    return ActiveRun.builder()
        .run(newRun(ol))
        .job(newJobWith(ol, namespace, jobName))
        .inputs(newInputs(ol, namespace))
        .outputs(newOutputs(ol, namespace))
        .build(ol, olClient);
  }

  /**
   * Returns new {@link ActiveRun} objects with a specified {@code namespace} and default {@code
   * limit}.
   */
  public static ImmutableList<ActiveRun> newOlActiveRuns(
      @NonNull final OpenLineage ol,
      @NonNull final OpenLineageClient olClient,
      @NonNull final String namespace) {
    return newOlActiveRunsFor(ol, olClient, namespace, newJobName().getValue(), 4);
  }

  /**
   * Returns new {@link ActiveRun} objects with a specified {@code namespace} and default {@code
   * limit}.
   */
  public static ImmutableList<ActiveRun> newOlActiveRunsFor(
      @NonNull final OpenLineage ol,
      @NonNull final OpenLineageClient olClient,
      @NonNull final String namespace,
      @NonNull final String jobName) {
    return newOlActiveRunsFor(ol, olClient, namespace, jobName, 4);
  }

  /** Returns new {@link ActiveRun} objects with a specified {@code namespace} and {@code limit}. */
  public static ImmutableList<ActiveRun> newOlActiveRunsFor(
      @NonNull final OpenLineage ol,
      @NonNull final OpenLineageClient olClient,
      @NonNull final String namespace,
      @NonNull final String jobName,
      final int totalRunsForJob) {
    return Stream.generate(() -> newOlActiveRunFor(ol, olClient, namespace, jobName))
        .limit(totalRunsForJob)
        .collect(toImmutableList());
  }

  /** ... */
  public static Set<OpenLineage.RunEvent> newRunEvents(
      @NonNull final OpenLineage ol,
      @NonNull final Instant now,
      @NonNull final String namespaceName,
      @NonNull final String jobName,
      final int limit) {
    return Stream.generate(() -> newRunEvent(ol, now, namespaceName, jobName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  /**
   * Returns a new {@link OpenLineage.Run} object; no {@code parent} run will be associated with
   * {@code child} run.
   */
  public static OpenLineage.RunEvent newRunEvent(
      @NonNull final OpenLineage ol,
      @NonNull final Instant now,
      @NonNull final String namespaceName,
      @NonNull final String jobName) {
    return ol.newRunEventBuilder()
        .eventType(COMPLETE)
        .eventTime(now.atZone(ZoneId.of("America/Los_Angeles")))
        .run(newRun(ol))
        .job(newJobWith(ol, namespaceName, jobName))
        .inputs(newInputs(ol, namespaceName))
        .outputs(newOutputs(ol, namespaceName))
        .build();
  }

  /**
   * Returns a new {@link OpenLineage.Run} object; no {@code parent} run will be associated with
   * {@code child} run.
   */
  public static Set<OpenLineage.Run> newRuns(@NonNull final OpenLineage ol, final int limit) {
    return Stream.generate(() -> newRun(ol)).limit(limit).collect(toImmutableSet());
  }

  /**
   * Returns a new {@link OpenLineage.Run} object; no {@code parent} run will be associated with
   * {@code child} run.
   */
  public static OpenLineage.Run newRun(@NonNull final OpenLineage ol) {
    return newRun(ol, false);
  }

  /**
   * Returns a new {@link OpenLineage.Run} object. A {@code parent} run will be associated with
   * {@code child} run if {@code hasParentRun} is {@code true}; otherwise, the {@code child} run
   * will not have a {@code parent} run.
   */
  public static OpenLineage.Run newRun(@NonNull final OpenLineage ol, final boolean hasParentRun) {
    return ol.newRun(
        newRunId().getValue(),
        ol.newRunFacetsBuilder()
            .parent(
                hasParentRun
                    ? ol.newParentRunFacetBuilder()
                        .run(newParentRun(ol))
                        .job(newParentJob(ol))
                        .build()
                    : null)
            .nominalTime(
                ol.newNominalTimeRunFacetBuilder()
                    .nominalStartTime(newNominalTime())
                    .nominalEndTime(newNominalTime().plusHours(1))
                    .build())
            .build());
  }

  /** Returns a new {@link OpenLineage.ParentRunFacetRun} object. */
  static OpenLineage.ParentRunFacetRun newParentRun(@NonNull final OpenLineage ol) {
    return ol.newParentRunFacetRunBuilder().runId(newRunId().getValue()).build();
  }

  /** Returns a new {@link OpenLineage.Job} object with a specified {@code namespace}. */
  static OpenLineage.Job newJob(@NonNull final OpenLineage ol, @NonNull final String namespace) {
    return newJobWith(ol, namespace, newJobName().getValue());
  }

  /** Returns a new {@link OpenLineage.Job} object with a specified {@code namespace}. */
  static OpenLineage.Job newJobWith(
      @NonNull final OpenLineage ol, @NonNull final String namespace, @NonNull String jobName) {
    return ol.newJobBuilder().namespace(namespace).name(jobName).build();
  }

  /** Returns a new {@link OpenLineage.ParentRunFacetJob} object. */
  static OpenLineage.ParentRunFacetJob newParentJob(@NonNull final OpenLineage ol) {
    return ol.newParentRunFacetJobBuilder()
        .namespace(newNamespaceName().getValue())
        .name(newJobName().getValue())
        .build();
  }

  /** ... */
  static List<OpenLineage.InputDataset> newInputs(
      @NonNull final OpenLineage ol, @NonNull final String namespace) {
    return newInputs(ol, namespace, 4, 2);
  }

  /** ... */
  static List<OpenLineage.InputDataset> newInputs(
      @NonNull final OpenLineage ol,
      @NonNull final String namespace,
      final int numOfInputs,
      final int numOfFields) {
    return Stream.generate(
            () ->
                ol.newInputDatasetBuilder()
                    .namespace(namespace)
                    .name(newDatasetName().getValue())
                    .facets(
                        ol.newDatasetFacetsBuilder()
                            .schema(newDatasetSchema(ol, numOfFields))
                            .build())
                    .build())
        .limit(numOfInputs)
        .collect(toImmutableList());
  }

  /** ... */
  static List<OpenLineage.OutputDataset> newOutputs(
      @NonNull final OpenLineage ol, @NonNull final String namespace) {
    return newOutputs(ol, namespace, 4, 1);
  }

  /** ... */
  static List<OpenLineage.OutputDataset> newOutputs(
      @NonNull final OpenLineage ol,
      @NonNull final String namespace,
      final int numOfOutputs,
      final int numOfFields) {
    return Stream.generate(
            () ->
                ol.newOutputDatasetBuilder()
                    .namespace(namespace)
                    .name(newDatasetName().getValue())
                    .facets(
                        ol.newDatasetFacetsBuilder()
                            .schema(newDatasetSchema(ol, numOfFields))
                            .build())
                    .build())
        .limit(numOfOutputs)
        .collect(toImmutableList());
  }

  /** ... */
  static OpenLineage.SchemaDatasetFacet newDatasetSchema(
      @NonNull final OpenLineage ol, final int numOfFields) {
    return ol.newSchemaDatasetFacetBuilder().fields(newFields(ol, numOfFields)).build();
  }

  static List<OpenLineage.SchemaDatasetFacetFields> newFields(
      @NonNull final OpenLineage ol, final int numOfFields) {
    return Stream.generate(
            () ->
                ol.newSchemaDatasetFacetFieldsBuilder()
                    .name(newFieldName().getValue())
                    .type(newFieldType())
                    .description(newDescription())
                    .build())
        .limit(numOfFields)
        .collect(toImmutableList());
  }

  /** ... */
  static ZonedDateTime newNominalTime() {
    return newNominalTimeWith(ZoneId.of("America/Los_Angeles"));
  }

  /** ... */
  static ZonedDateTime newNominalTimeWith(@NonNull final ZoneId zoneId) {
    return Instant.now().atZone(zoneId);
  }

  static URI newSchemaUrl() {
    return URI.create("http://test.com/schema");
  }

  static URI newProducer() {
    return URI.create("http://test.com/producer");
  }

  static Instant newNominalStartTime() {
    return ZonedDateTime.of(2024, 10, 7, 10, 0, 0, 0, ZoneOffset.UTC).toInstant();
  }
}
