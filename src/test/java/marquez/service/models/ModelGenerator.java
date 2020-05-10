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
import static marquez.common.models.ModelGenerator.newConnectionUrlFor;
import static marquez.common.models.ModelGenerator.newContext;
import static marquez.common.models.ModelGenerator.newDatasetNames;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newJobName;
import static marquez.common.models.ModelGenerator.newJobType;
import static marquez.common.models.ModelGenerator.newLocation;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newOwnerName;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.common.models.ModelGenerator.newSourceType;
import static marquez.common.models.ModelGenerator.newTagName;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import marquez.Generator;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;

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
    final SourceType sourceType = newSourceType();
    return new Source(
        sourceType, sourceName, now, now, newConnectionUrlFor(sourceType), newDescription());
  }

  public static ImmutableSet<Tag> newTags(final int limit) {
    return Stream.generate(() -> newTag()).limit(limit).collect(toImmutableSet());
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
        newDescription());
  }

  public static Job newJob() {
    return newJobWith(newJobName(), 4, 2);
  }

  public static Job newJobWith(final JobName jobName) {
    return newJobWith(jobName, 4, 2);
  }

  public static Job newJobWith(
      final JobName jobName, final int numOfInputs, final int numOfOutputs) {
    final Instant now = Instant.now();
    final JobMeta jobMeta = newJobMetaWith(numOfInputs, numOfOutputs);
    return new Job(
        jobMeta.getType(),
        jobName,
        now,
        now,
        jobMeta.getInputs(),
        jobMeta.getOutputs(),
        jobMeta.getLocation().orElse(null),
        jobMeta.getContext(),
        jobMeta.getDescription().orElse(null),
        null);
  }

  public static Tag newTag() {
    return new Tag(newTagName(), newDescription());
  }

  public static List<DatasetName> newInputs(final int limit) {
    return newDatasetNames(limit);
  }

  public static List<DatasetName> newOutputs(final int limit) {
    return newDatasetNames(limit);
  }
}
