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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public final class JobRunRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  @Getter @NonNull private final UUID jobVersionUuid;
  @Getter @NonNull private final String currentRunState;
  @Getter @NonNull private final List<UUID> inputDatasetVersionUuids;
  @Getter @NonNull private final List<UUID> outputDatasetVersionUuids;
  private final Instant nominalStartTime;
  private final Instant nominalEndTime;

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }
}
