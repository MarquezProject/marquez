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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.RunId;
import marquez.common.models.RunState;

@EqualsAndHashCode
@ToString
public final class Run {
  @Getter private final RunId id;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Nullable private final Instant nominalStartTime;
  @Nullable private final Instant nominalEndTime;
  @Getter private final RunState state;
  @Nullable private final Instant startedAt;
  @Nullable private final Instant endedAt;
  @Nullable private final Long durationMs;
  @Getter private final Map<String, String> args;
  private final String namespaceName;
  private final String jobName;

  public Run(
      @NonNull final RunId id,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @NonNull final RunState state,
      @Nullable final Instant startedAt,
      @Nullable final Instant endedAt,
      @Nullable final Long durationMs,
      @Nullable final Map<String, String> args,
      @NonNull String namespaceName,
      @NonNull String jobName) {
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.state = state;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.durationMs = durationMs;
    this.args = (args == null) ? ImmutableMap.of() : args;
    this.namespaceName = namespaceName;
    this.jobName = jobName;
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

  @JsonIgnore
  public String getNamespaceName() {
    return namespaceName;
  }

  @JsonIgnore
  public String getJobName() {
    return jobName;
  }
}
