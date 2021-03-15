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

package marquez.db.models;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RunRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  private final UUID jobVersionUuid;
  @Getter @NonNull private final UUID runArgsUuid;
  @Getter @NonNull private final List<UUID> inputVersionUuids;
  @Nullable private final Instant nominalStartTime;
  @Nullable private final Instant nominalEndTime;
  @Nullable private final String currentRunState;
  @Nullable private final Instant startedAt;
  @Nullable private final UUID startRunStateUuid;
  @Nullable private final Instant endedAt;
  @Nullable private final UUID endRunStateUuid;
  @Getter private final String namespaceName;
  @Getter private final String jobName;

  public boolean hasInputVersionUuids() {
    return !inputVersionUuids.isEmpty();
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public Optional<String> getCurrentRunState() {
    return Optional.ofNullable(currentRunState);
  }

  public Optional<Instant> getStartedAt() {
    return Optional.ofNullable(startedAt);
  }

  public Optional<UUID> getStartRunStateUuid() {
    return Optional.ofNullable(startRunStateUuid);
  }

  public Optional<Instant> getEndedAt() {
    return Optional.ofNullable(endedAt);
  }

  public Optional<UUID> getEndRunStateUuid() {
    return Optional.ofNullable(endRunStateUuid);
  }
}
