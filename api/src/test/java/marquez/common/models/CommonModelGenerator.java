/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import marquez.Generator;
import marquez.common.Utils;
import marquez.service.models.LineageEvent;

public final class CommonModelGenerator extends Generator {
  private CommonModelGenerator() {}

  public static NamespaceName newNamespaceName() {
    return NamespaceName.of("s3://test_namespace" + newId());
  }

  public static OwnerName newOwnerName() {
    return OwnerName.of("test_owner" + newId());
  }

  public static SourceType newDbSourceType() {
    return SourceType.of("POSTGRESQL");
  }

  public static SourceType newStreamSourceType() {
    return SourceType.of("KAFKA");
  }

  public static SourceName newSourceName() {
    return SourceName.of("test_source" + newId());
  }

  public static URI newConnectionUrl() {
    return newConnectionUrlFor(newDbSourceType());
  }

  public static URI newConnectionUrlFor(SourceType type) {
    if ("POSTGRESQL".equalsIgnoreCase(type.getValue())) {
      return URI.create("postgresql://localhost:5432/test" + newId());
    } else if ("KAFKA".equalsIgnoreCase(type.getValue())) {
      return URI.create("kafka://localhost:9092");
    } else {
      return URI.create(String.format("%s://localhost:9092", type.getValue()));
    }
  }

  public static ImmutableSet<DatasetId> newDatasetIds(final int limit) {
    return newDatasetIdsWith(newNamespaceName(), limit);
  }

  public static ImmutableSet<DatasetId> newDatasetIdsWith(
      final NamespaceName namespaceName, final int limit) {
    return Stream.generate(() -> newDatasetIdWith(namespaceName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static DatasetId newDatasetId() {
    return newDatasetIdWith(newNamespaceName());
  }

  public static DatasetId newDatasetIdWith(final NamespaceName namespaceName) {
    return new DatasetId(namespaceName, newDatasetName());
  }

  public static ImmutableSet<DatasetName> newDatasetNames(final int limit) {
    return Stream.generate(CommonModelGenerator::newDatasetName)
        .limit(limit)
        .collect(toImmutableSet());
  }

  public static DatasetName newDatasetName() {
    return DatasetName.of("test_dataset" + newId());
  }

  public static DatasetType newDatasetType() {
    return DatasetType.values()[newIdWithBound(DatasetType.values().length - 1)];
  }

  public static Field newField() {
    return new Field(newFieldName(), newFieldType(), newTagNames(2), newDescription());
  }

  public static LineageEvent.SchemaField newSchemaField() {
    return new LineageEvent.SchemaField(
        newFieldName().getValue(), newFieldType(), newDescription());
  }

  public static List<LineageEvent.SchemaField> newSchemaFields(int amount) {
    return IntStream.range(0, amount)
        .boxed()
        .map(
            i ->
                new LineageEvent.SchemaField(
                    newFieldName().getValue(), newFieldType(), newDescription()))
        .collect(Collectors.toList());
  }

  public static FieldName newFieldName() {
    return FieldName.of("test_field" + newId());
  }

  public static String newFieldType() {
    return String.format("VARCHAR(%d)", newIdWithBound(255));
  }

  public static ImmutableList<Field> newFields(final int limit) {
    return Stream.generate(CommonModelGenerator::newField).limit(limit).collect(toImmutableList());
  }

  public static ImmutableSet<TagName> newTagNames(final int limit) {
    return Stream.generate(CommonModelGenerator::newTagName).limit(limit).collect(toImmutableSet());
  }

  public static TagName newTagName() {
    return TagName.of("test_tag" + newId());
  }

  public static JobId newJobId() {
    return newJobIdWith(newNamespaceName());
  }

  public static JobId newJobIdWith(final NamespaceName namespaceName) {
    return new JobId(namespaceName, newJobName());
  }

  public static JobName newJobName() {
    return JobName.of("test_job" + newId());
  }

  public static JobType newJobType() {
    return JobType.values()[newIdWithBound(JobType.values().length - 1)];
  }

  public static URL newLocation() {
    return Utils.toUrl("https://github.com/repo/test/commit/" + newId());
  }

  public static ImmutableMap<String, String> newContext() {
    return ImmutableMap.of(
        "sql", String.format("SELECT * FROM test_table WHERE test_column = '%dH';", newId()));
  }

  public static RunId newRunId() {
    return RunId.of(UUID.randomUUID());
  }

  public static String newLifecycleState() {
    return "TRUNCATE";
  }

  public static Version newVersion() {
    return Version.of(UUID.randomUUID());
  }

  public static String newDescription() {
    return "test_description" + newId();
  }

  public static URL newSchemaLocation() {
    return Utils.toUrl("http://localhost:8081/schemas/ids/" + newId());
  }

  public static String newExternalId() {
    return "test_external_id" + newId();
  }
}
