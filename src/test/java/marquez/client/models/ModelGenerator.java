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

package marquez.client.models;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static marquez.client.models.SourceType.POSTGRESQL;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class ModelGenerator {
  private ModelGenerator() {}

  private static final Random RANDOM = new Random();

  public static NamespaceMeta newNamespaceMeta() {
    return NamespaceMeta.builder().ownerName(newOwnerName()).description(newDescription()).build();
  }

  public static Namespace newNamespace() {
    final Instant now = newTimestamp();
    return new Namespace(newNamespaceName(), now, now, newOwnerName(), newDescription());
  }

  public static SourceMeta newSourceMeta() {
    return SourceMeta.builder()
        .type(POSTGRESQL)
        .connectionUrl(newConnectionUrl())
        .description(newDescription())
        .build();
  }

  public static DatasetMeta newDatasetMeta() {
    return newDatasetMetaWith(newDatasetType());
  }

  public static DatasetMeta newDatasetMetaWith(DatasetType type) {
    switch (type) {
      case STREAM:
        return newStreamMeta();
      default:
        return newDbTableMeta();
    }
  }

  public static DbTableMeta newDbTableMeta() {
    return DbTableMeta.builder()
        .physicalName(newDatasetPhysicalName())
        .sourceName(newSourceName())
        .description(newDescription())
        .build();
  }

  public static StreamMeta newStreamMeta() {
    return StreamMeta.builder()
        .physicalName(newDatasetPhysicalName())
        .sourceName(newSourceName())
        .description(newDescription())
        .schemaLocation(newSchemaLocation())
        .build();
  }

  public static Dataset newDataset() {
    return newDatasetWith(newDatasetType());
  }

  public static Dataset newDatasetWith(DatasetType type) {
    switch (type) {
      case STREAM:
        return newStream();
      default:
        return newDbTable();
    }
  }

  public static JobMeta newJobMeta() {
    return JobMeta.builder()
        .type(newJobType())
        .inputs(newInputs(2))
        .outputs(newOutputs(4))
        .location(newLocation())
        .description(newDescription())
        .build();
  }

  public static RunMeta newRunMeta() {
    return RunMeta.builder()
        .nominalStartTime(newTimestamp())
        .nominalEndTime(newTimestamp())
        .args(newRunArgs())
        .build();
  }

  public static Job newJob() {
    final Instant now = newTimestamp();
    return new Job(
        newJobType(),
        newJobName(),
        now,
        now,
        newInputs(2),
        newOutputs(4),
        newLocation().toString(),
        newDescription());
  }

  public static Run newRun() {
    final Instant now = newTimestamp();
    return new Run(newRunId(), now, now, now, now, newRunArgs(), Run.State.NEW);
  }

  public static Source newSource() {
    final Instant now = newTimestamp();
    return new Source(
        newSourceType(),
        newSourceName(),
        now,
        now,
        newConnectionUrl().toString(),
        newDescription());
  }

  public static DbTable newDbTable() {

    final Instant now = Instant.now();

    return new DbTable(
        newDatasetName(), newDatasetPhysicalName(), now, now, newSourceName(), newDescription());
  }

  public static Stream newStream() {
    final Instant now = newTimestamp();
    return new Stream(
        newDatasetName(),
        newDatasetPhysicalName(),
        now,
        now,
        newSourceName(),
        newSchemaLocation().toString(),
        newDescription());
  }

  public static List<Namespace> newNamespaces(int limit) {
    return java.util.stream.Stream.generate(() -> newNamespace())
        .limit(limit)
        .collect(toImmutableList());
  }

  public static List<Job> newJobs(int limit) {
    return java.util.stream.Stream.generate(() -> newJob()).limit(limit).collect(toImmutableList());
  }

  public static List<Run> newRuns(int limit) {
    return java.util.stream.Stream.generate(() -> newRun()).limit(limit).collect(toImmutableList());
  }

  public static List<Source> newSources(int limit) {
    return java.util.stream.Stream.generate(() -> newSource())
        .limit(limit)
        .collect(toImmutableList());
  }

  public static List<Dataset> newDatasets(int limit) {
    return java.util.stream.Stream.generate(() -> newDataset())
        .limit(limit)
        .collect(toImmutableList());
  }

  public static String newOwnerName() {
    return "test_owner" + newId();
  }

  public static String newNamespaceName() {
    return "test_namespace" + newId();
  }

  public static String newJobName() {
    return "test_job" + newId();
  }

  public static String newLocation() {
    return "https://github.com/repo/test/commit/" + newId();
  }

  public static String newSourceName() {
    return "test_source" + newId();
  }

  public static SourceType newSourceType() {
    return SourceType.values()[newIdWithBound(SourceType.values().length)];
  }

  public static String newConnectionUrl() {
    return "jdbc:postgresql://localhost:5431/test_db" + newId();
  }

  public static DatasetType newDatasetType() {
    return DatasetType.values()[newIdWithBound(DatasetType.values().length)];
  }

  public static String newDatasetName() {
    return "test_dataset" + newId();
  }

  public static String newDatasetPhysicalName() {
    return "test_schema.test_table" + newId();
  }

  public static String newSchemaLocation() {
    return "http://localhost:8081/schemas/ids/" + newId();
  }

  public static String newDescription() {
    return "test_description" + newId();
  }

  public static List<String> newInputs(int limit) {
    return newDatasetNames(limit);
  }

  public static List<String> newOutputs(int limit) {
    return newDatasetNames(limit);
  }

  private static List<String> newDatasetNames(int limit) {
    return java.util.stream.Stream.generate(() -> newDatasetName())
        .limit(limit)
        .collect(toImmutableList());
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

  public static JobType newJobType() {
    return JobType.values()[newIdWithBound(JobType.values().length)];
  }

  public static Instant newTimestamp() {
    return Instant.now();
  }
}
