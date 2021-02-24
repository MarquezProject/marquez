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

package marquez.service.models;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.time.temporal.ChronoUnit.HOURS;
import static marquez.common.models.ModelGenerator.newConnectionUrlFor;
import static marquez.common.models.ModelGenerator.newContext;
import static marquez.common.models.ModelGenerator.newDatasetId;
import static marquez.common.models.ModelGenerator.newDatasetIds;
import static marquez.common.models.ModelGenerator.newDatasetName;
import static marquez.common.models.ModelGenerator.newDbSourceType;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newFields;
import static marquez.common.models.ModelGenerator.newJobId;
import static marquez.common.models.ModelGenerator.newJobName;
import static marquez.common.models.ModelGenerator.newJobType;
import static marquez.common.models.ModelGenerator.newLocation;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newOwnerName;
import static marquez.common.models.ModelGenerator.newRunId;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.common.models.ModelGenerator.newTagName;
import static marquez.common.models.ModelGenerator.newTagNames;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import marquez.Generator;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.common.models.Version;

public final class ModelGenerator extends Generator {
  private ModelGenerator() {}

  public static NamespaceMeta newNamespaceMeta() {
    return new NamespaceMeta(newOwnerName(), newDescription());
  }

  public static Namespace newNamespace() {
    return newNamespaceWith(newNamespaceName());
  }

  public static Namespace newNamespaceWith(final NamespaceName namespaceName) {
    final Instant now = newTimestamp();
    return new Namespace(namespaceName, now, now, newOwnerName(), newDescription());
  }

  public static Source newSource() {
    return newSourceWith(newSourceName());
  }

  public static Source newSourceWith(final SourceName sourceName) {
    final Instant now = newTimestamp();
    final SourceType sourceType = newDbSourceType();
    return new Source(
        sourceType, sourceName, now, now, newConnectionUrlFor(sourceType), newDescription());
  }

  public static DbTable newDbTable() {
    return newDbTableWith(newDatasetId());
  }

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
        newDescription());
  }

  public static DbTableMeta newDbTableMeta() {
    return newDbTableMetaWith(null);
  }

  public static DbTableMeta newDbTableMetaWith(@Nullable final RunId runId) {
    return new DbTableMeta(
        newDatasetName(), newSourceName(), newFields(4), newTagNames(2), newDescription(), runId);
  }

  public static JobMeta newJobMeta() {
    return newJobMetaWith(4, 2);
  }

  public static JobMeta newJobMetaWith(final int numOfInputs, final int numOfOutputs) {
    return new JobMeta(
        newJobType(),
        newInputs(numOfInputs),
        newOutputs(numOfOutputs),
        newLocation(),
        newContext(),
        newDescription(),
        null);
  }

  public static Job newJob() {
    return newJobWith(newJobId(), 4, 2);
  }

  public static Job newJobWith(final JobId jobId) {
    return newJobWith(jobId, 4, 2);
  }

  public static Job newJobWith(final JobId jobId, final int numOfInputs, final int numOfOutputs) {
    final Instant now = newTimestamp();
    final JobMeta jobMeta = newJobMetaWith(numOfInputs, numOfOutputs);
    return new Job(
        jobId,
        jobMeta.getType(),
        jobId.getName(),
        now,
        now,
        jobMeta.getInputs(),
        jobMeta.getOutputs(),
        jobMeta.getLocation().orElse(null),
        jobMeta.getContext(),
        jobMeta.getDescription().orElse(null),
        null);
  }

  public static RunMeta newRunMeta() {
    return newRunMeta(null);
  }

  public static RunMeta newRunMeta(RunId runId) {
    final Instant nominalStartTime = newTimestamp();
    final Instant nominalEndTime = nominalStartTime.plus(1, HOURS);
    return new RunMeta(runId, nominalStartTime, nominalEndTime, newRunArgs());
  }

  public static Run newRun() {
    return newRunWith(
        newRunId(), newRunState(), newIsoTimestamp(), newNamespaceName(), newJobName());
  }

  public static Run newRunWith(final RunId runId) {
    return newRunWith(runId, newRunState(), newIsoTimestamp(), newNamespaceName(), newJobName());
  }

  public static Run newRunWith(final RunState runState) {
    return newRunWith(newRunId(), runState, newIsoTimestamp(), newNamespaceName(), newJobName());
  }

  public static Run newRunWith(
      final RunId runId,
      final RunState runState,
      final String transitionedAt,
      NamespaceName namespaceName,
      JobName jobName) {
    final Instant now = Utils.toInstant(transitionedAt);
    final RunMeta runMeta = newRunMeta();
    return new Run(
        runId,
        now,
        now,
        runMeta.getNominalStartTime().orElse(null),
        runMeta.getNominalEndTime().orElse(null),
        runState,
        null,
        null,
        null,
        newRunArgs(),
        namespaceName.getValue(),
        jobName.getValue());
  }

  public static RunState newRunState() {
    return RunState.values()[newIdWithBound(RunState.values().length - 1)];
  }

  public static ImmutableMap<String, String> newRunArgs() {
    return newRunArgsWith(4);
  }

  public static ImmutableMap<String, String> newRunArgsWith(final int limit) {
    final ImmutableMap.Builder<String, String> runArgs = ImmutableMap.builder();
    IntStream.range(1, limit).forEach(i -> runArgs.put(runArgKey(i), runArgValue(i)));
    return runArgs.build();
  }

  private static String runArgKey(final int i) {
    return "test_key" + i + newId();
  }

  private static String runArgValue(final int i) {
    return "test_value" + i + newId();
  }

  public static ImmutableSet<Tag> newTags(final int limit) {
    return Stream.generate(ModelGenerator::newTag).limit(limit).collect(toImmutableSet());
  }

  public static Tag newTag() {
    return new Tag(newTagName(), newDescription());
  }

  public static ImmutableSet<DatasetId> newInputs(final int limit) {
    return newDatasetIds(limit);
  }

  public static ImmutableSet<DatasetId> newOutputs(final int limit) {
    return newDatasetIds(limit);
  }

  public static URL newSchemaLocation() {
    return Utils.toUrl("http://localhost:8081/schemas/ids/" + newId());
  }

  public static Version newVersion() {
    return Version.of(UUID.randomUUID());
  }
}
