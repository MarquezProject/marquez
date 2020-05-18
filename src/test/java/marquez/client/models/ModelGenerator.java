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
import java.net.URL;
import java.time.Instant;
import java.util.*;
import marquez.client.Utils;

public final class ModelGenerator {
  private ModelGenerator() {}

  private static final Random RANDOM = new Random();

  public static NamespaceMeta newNamespaceMeta() {
    return NamespaceMeta.builder().ownerName(newOwnerName()).description(newDescription()).build();
  }

  public static List<Namespace> newNamespaces(final int limit) {
    return java.util.stream.Stream.generate(() -> newNamespace())
        .limit(limit)
        .collect(toImmutableList());
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

  public static List<Source> newSources(final int limit) {
    return java.util.stream.Stream.generate(() -> newSource())
        .limit(limit)
        .collect(toImmutableList());
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

  public static DbTableMeta newDbTableMeta() {
    return DbTableMeta.builder()
        .physicalName(newDatasetPhysicalName())
        .sourceName(newSourceName())
        .fields(newFields())
        .tags(newTags())
        .description(newDescription())
        .build();
  }

  public static DbTable newDbTable() {
    final Instant now = newTimestamp();
    return new DbTable(
        newDatasetName(),
        newDatasetPhysicalName(),
        now,
        now,
        newSourceName(),
        newFields(),
        newTags(),
        null,
        newDescription());
  }

  public static StreamMeta newStreamMeta() {
    return StreamMeta.builder()
        .physicalName(newStreamName())
        .sourceName(newSourceName())
        .fields(newFields())
        .tags(newTags())
        .description(newDescription())
        .schemaLocation(newSchemaLocation())
        .build();
  }

  public static Stream newStream() {
    final Instant now = newTimestamp();
    return new Stream(
        newDatasetName(),
        newStreamName(),
        now,
        now,
        newSourceName(),
        newFields(),
        newTags(),
        null,
        newSchemaLocation(),
        newDescription());
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
    return java.util.stream.Stream.generate(() -> newJob()).limit(limit).collect(toImmutableList());
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
        newDescription(),
        newContext());
  }

  public static RunMeta newRunMeta() {
    return RunMeta.builder()
        .nominalStartTime(newTimestamp())
        .nominalEndTime(newTimestamp())
        .args(newRunArgs())
        .build();
  }

  public static List<Run> newRuns(final int limit) {
    return java.util.stream.Stream.generate(() -> newRun()).limit(limit).collect(toImmutableList());
  }

  public static Run newRun() {
    final Instant now = newTimestamp();
    return new Run(newRunId(), now, now, now, now, Run.State.NEW, newRunArgs());
  }

  public static String newOwnerName() {
    return "test_owner" + newId();
  }

  public static String newNamespaceName() {
    return "test_namespace" + newId();
  }

  public static SourceType newSourceType() {
    return SourceType.values()[newIdWithBound(SourceType.values().length)];
  }

  public static String newSourceName() {
    return "test_source" + newId();
  }

  public static String newConnectionUrl() {
    return "jdbc:postgresql://localhost:5431/test_db" + newId();
  }

  public static String newDatasetName() {
    return "test_dataset" + newId();
  }

  private static List<String> newDatasetNames(final int limit) {
    return java.util.stream.Stream.generate(() -> newDatasetName())
        .limit(limit)
        .collect(toImmutableList());
  }

  public static String newDatasetPhysicalName() {
    return "test_schema.test_table" + newId();
  }

  public static String newStreamName() {
    return "test." + newId();
  }

  public static String newHttpPath() {
    return "/test/" + newId();
  }

  public static JobType newJobType() {
    return JobType.values()[newIdWithBound(JobType.values().length)];
  }

  public static String newJobName() {
    return "test_job" + newId();
  }

  public static List<String> newInputs(final int limit) {
    return newDatasetNames(limit);
  }

  public static List<String> newOutputs(final int limit) {
    return newDatasetNames(limit);
  }

  public static String newLocation() {
    return "https://github.com/repo/test/commit/" + newId();
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

  public static Run.State newRunState() {
    return Run.State.values()[newIdWithBound(Run.State.values().length)];
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

  public static List<Field> newFields() {
    return new ArrayList<>();
  }

  public static List<String> newTags() {
    return new ArrayList<>();
  }
}
