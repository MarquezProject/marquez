/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import marquez.client.Utils;

public final class ModelGenerator {
  private ModelGenerator() {}

  private static final Random RANDOM = new Random();

  public static NamespaceMeta newNamespaceMeta() {
    return NamespaceMeta.builder().ownerName(newOwnerName()).description(newDescription()).build();
  }

  public static List<Namespace> newNamespaces(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newNamespace)
        .limit(limit)
        .collect(toList());
  }

  public static Namespace newNamespace() {
    final Instant now = newTimestamp();
    return new Namespace(newNamespaceName(), now, now, newOwnerName(), newDescription());
  }

  public static SourceMeta newSourceMeta() {
    return SourceMeta.builder()
        .type("postgresql")
        .connectionUrl(newConnectionUrl())
        .description(newDescription())
        .build();
  }

  public static List<Source> newSources(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newSource)
        .limit(limit)
        .collect(toList());
  }

  public static Source newSource() {
    final Instant now = newTimestamp();
    return new Source(
        newSourceType(), newSourceName(), now, now, newConnectionUrl(), newDescription());
  }

  public static DbTableMeta newDbTableMeta() {
    return DbTableMeta.builder()
        .physicalName(newDatasetPhysicalName())
        .sourceName(newSourceName())
        .fields(newFields(2))
        .tags(newTagNames(2))
        .description(newDescription())
        .build();
  }

  public static DbTable newDbTableWith(UUID currentVersion) {
    final Instant now = newTimestamp();
    final DatasetId dbTableId = newDatasetId();
    return new DbTable(
        dbTableId,
        dbTableId.getName(),
        newDatasetPhysicalName(),
        now,
        now,
        dbTableId.getNamespace(),
        newSourceName(),
        newFields(2),
        newTagNames(2),
        null,
        newDescription(),
        newDatasetFacets(2),
        currentVersion);
  }

  public static DbTableVersion newDbTableVersion() {
    final Instant now = newTimestamp();
    final DatasetId dbTableId = newDatasetId();
    return new DbTableVersion(
        dbTableId,
        dbTableId.getName(),
        newDatasetPhysicalName(),
        now,
        newVersion(),
        newSourceName(),
        newFields(2),
        newTagNames(2),
        newDescription(),
        newRun(),
        newDatasetFacets(2));
  }

  public static StreamMeta newStreamMeta() {
    return StreamMeta.builder()
        .physicalName(newStreamName())
        .sourceName(newSourceName())
        .fields(newFields(2))
        .tags(newTagNames(2))
        .description(newDescription())
        .schemaLocation(newSchemaLocation())
        .build();
  }

  public static Stream newStreamWith(UUID currentVersion) {
    final Instant now = newTimestamp();
    final DatasetId streamId = newDatasetId();
    return new Stream(
        streamId,
        streamId.getName(),
        newStreamName(),
        now,
        now,
        streamId.getNamespace(),
        newSourceName(),
        newFields(2),
        newTagNames(2),
        null,
        newSchemaLocation(),
        newDescription(),
        newDatasetFacets(2),
        currentVersion);
  }

  public static StreamVersion newStreamVersion() {
    final Instant now = newTimestamp();
    final DatasetId streamId = newDatasetId();
    return new StreamVersion(
        streamId,
        streamId.getName(),
        newStreamName(),
        now,
        newVersion(),
        newSourceName(),
        newFields(2),
        newTagNames(2),
        newSchemaLocation(),
        newDescription(),
        newRun(),
        newDatasetFacets(2));
  }

  public static Set<DatasetId> newDatasetIds(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newDatasetId)
        .limit(limit)
        .collect(toSet());
  }

  public static DatasetId newDatasetId() {
    return newDatasetIdWith(newNamespaceName());
  }

  public static DatasetId newDatasetIdWith(final String namespaceName) {
    return new DatasetId(namespaceName, newDatasetName());
  }

  public static JobMeta newJobMeta() {
    return JobMeta.builder()
        .type(newJobType())
        .inputs(newInputs(2))
        .outputs(newOutputs(4))
        .location(newLocation())
        .description(newDescription())
        .context(newContext())
        .build();
  }

  public static List<Job> newJobs(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newJob).limit(limit).collect(toList());
  }

  public static Job newJob() {
    return newJobWith(null, null);
  }

  public static Job newJobWith(final Run latestRun, UUID currentVersion) {
    final Instant now = newTimestamp();
    final JobId jobId = newJobId();
    return new Job(
        jobId,
        newJobType(),
        jobId.getName(),
        jobId.getName(),
        null,
        now,
        now,
        jobId.getNamespace(),
        newInputs(2),
        newOutputs(4),
        newLocation(),
        newContext(),
        newDescription(),
        latestRun,
        null,
        currentVersion);
  }

  public static Job newJobWith(final UUID currentVersion) {
    return newJobWith(null, currentVersion);
  }

  public static JobId newJobId() {
    return newJobIdWith(newNamespaceName());
  }

  public static JobId newJobIdWith(final String namespaceName) {
    return new JobId(namespaceName, newJobName());
  }

  public static RunMeta newRunMeta() {
    return RunMeta.builder()
        .nominalStartTime(newTimestamp())
        .nominalEndTime(newTimestamp())
        .args(newRunArgs())
        .build();
  }

  public static List<Run> newRuns(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newRun).limit(limit).collect(toList());
  }

  public static Run newRun() {
    final Instant now = newTimestamp();
    return new Run(
        newRunId(), now, now, now, now, RunState.NEW, null, null, null, newRunArgs(), null);
  }

  public static String newOwnerName() {
    return "test_owner" + newId();
  }

  public static String newNamespaceName() {
    return "test_namespace" + newId();
  }

  public static String newSourceType() {
    return "test_source_type" + newId();
  }

  public static String newSourceName() {
    return "test_source" + newId();
  }

  public static URI newConnectionUrl() {
    return URI.create("jdbc:postgresql://localhost:5431/test_db" + newId());
  }

  public static String newDatasetName() {
    return "test_dataset" + newId();
  }

  private static Set<String> newDatasetNames(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newDatasetName)
        .limit(limit)
        .collect(toSet());
  }

  public static String newDatasetPhysicalName() {
    return "test_schema.test_table" + newId();
  }

  public static String newStreamName() {
    return "test." + newId();
  }

  public static JobType newJobType() {
    return JobType.values()[newIdWithBound(JobType.values().length)];
  }

  public static String newJobName() {
    return "test_job" + newId();
  }

  public static Set<DatasetId> newInputs(final int limit) {
    return newDatasetIds(limit);
  }

  public static Set<DatasetId> newOutputs(final int limit) {
    return newDatasetIds(limit);
  }

  public static URL newLocation() {
    return Utils.toUrl(
        String.format("https://github.com/test-org/test-repo/blob/%s/test.java", newId()));
  }

  public static Map<String, String> newContext() {
    return ImmutableMap.of(
        "sql", String.format("SELECT * FROM room_bookings WHERE room = '%dH';", newId()));
  }

  public static URL newSchemaLocation() {
    return Utils.toUrl("http://localhost:8081/schemas/ids/" + newId());
  }

  public static String newDescription() {
    return "test_description" + newId();
  }

  public static RunState newRunState() {
    return RunState.values()[newIdWithBound(RunState.values().length)];
  }

  public static Map<String, String> newRunArgs() {
    return ImmutableMap.of("email", String.format("wedata%s@wework.com", newId()));
  }

  public static String newRunId() {
    return UUID.randomUUID().toString();
  }

  private static int newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  private static int newIdWithBound(final int bound) {
    return RANDOM.nextInt(bound);
  }

  public static Instant newTimestamp() {
    return Instant.now();
  }

  public static List<Field> newFields(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newField)
        .limit(limit)
        .collect(toList());
  }

  public static Field newField() {
    return Field.builder()
        .name(newFieldName())
        .type(newFieldType())
        .tags(newTagNames(2))
        .description(newDescription())
        .build();
  }

  public static String newFieldName() {
    return "test_field" + newId();
  }

  public static String newFieldType() {
    return "test_field_type" + newId();
  }

  public static Set<String> newTagNames(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newTagName)
        .limit(limit)
        .collect(toSet());
  }

  public static String newTagName() {
    return "test_tag" + newId();
  }

  public static String newVersion() {
    return UUID.randomUUID().toString();
  }

  public static Map<String, Object> newDatasetFacets(final int limit) {
    return java.util.stream.Stream.generate(ModelGenerator::newDatasetFacet)
        .limit(limit)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static Map.Entry<String, Object> newDatasetFacet() {
    return new AbstractMap.SimpleImmutableEntry<>(newFacetName(), newFacetFields());
  }

  public static String newFacetName() {
    return "test_facet" + newId();
  }

  public static Map<Object, Object> newFacetFields() {
    return ImmutableMap.builder().put(newFacetProducer()).put(newFacetSchemaURL()).build();
  }

  public static Map.Entry<String, String> newFacetProducer() {
    return new AbstractMap.SimpleImmutableEntry<>("_producer", "test_producer" + newId());
  }

  public static Map.Entry<String, String> newFacetSchemaURL() {
    return new AbstractMap.SimpleImmutableEntry<>("_schemaURL", "test_schemaURL" + newId());
  }
}
