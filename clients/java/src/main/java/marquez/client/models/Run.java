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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public final class Run {
  @Getter private final String id;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Nullable private final Instant nominalStartTime;
  @Nullable private final Instant nominalEndTime;
  @Getter private final RunState state;
  @Nullable private final Instant startedAt;
  @Nullable private final Instant endedAt;
  @Nullable private final Long durationMs;
  @Getter private final Map<String, String> args;
  @Getter private final Map<String, Object> facets;
  @Getter private final JobVersionId jobVersionId;
  @Getter private final Set<DatasetVersionId> inputVersions;
  @Getter private final Set<DatasetVersionId> outputVersions;

  public Run(
      @NonNull final String id,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @NonNull final RunState state,
      @Nullable final Instant startedAt,
      @Nullable final Instant endedAt,
      @Nullable final Long durationMs,
      @Nullable final Map<String, String> args,
      @Nullable final Map<String, Object> facets,
      @NonNull final JobVersionId jobVersionId,
      @Nullable final Set<DatasetVersionId> inputVersions,
      @Nullable final Set<DatasetVersionId> outputVersions) {
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.state = state;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.durationMs = durationMs;
    this.args = (args == null) ? ImmutableMap.of() : ImmutableMap.copyOf(args);
    this.facets = (facets == null) ? ImmutableMap.of() : ImmutableMap.copyOf(facets);
    this.jobVersionId = jobVersionId;
    this.inputVersions =
        (inputVersions == null) ? ImmutableSet.of() : ImmutableSet.copyOf(inputVersions);
    this.outputVersions =
        (outputVersions == null) ? ImmutableSet.of() : ImmutableSet.copyOf(outputVersions);
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public Optional<Instant> getStartedAt() {
    return Optional.ofNullable(startedAt);
  }

  public Optional<Instant> getEndedAt() {
    return Optional.ofNullable(endedAt);
  }

  public Optional<Long> getDurationMs() {
    return Optional.ofNullable(durationMs);
  }

  public boolean hasFacets() {
    return !facets.isEmpty();
  }

  public static Run fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<Run>() {});
  }
}
