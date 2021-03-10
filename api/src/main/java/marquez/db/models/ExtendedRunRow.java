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
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExtendedRunRow extends RunRow {
  @Getter private final String args;

  public ExtendedRunRow(
      final UUID uuid,
      final Instant createdAt,
      final Instant updatedAt,
      final UUID jobVersionUuid,
      final UUID runArgsUuid,
      final List<UUID> inputVersionUuids,
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @Nullable final String currentRunState,
      @Nullable final Instant startedAt,
      @Nullable final UUID startRunStateUuid,
      @Nullable final Instant endedAt,
      @Nullable final UUID endRunStateUuid,
      @NonNull final String args,
      @NonNull final String namespaceName,
      @NonNull final String jobName) {
    super(
        uuid,
        createdAt,
        updatedAt,
        jobVersionUuid,
        runArgsUuid,
        inputVersionUuids,
        nominalStartTime,
        nominalEndTime,
        currentRunState,
        startedAt,
        startRunStateUuid,
        endedAt,
        endRunStateUuid,
        namespaceName,
        jobName);
    this.args = args;
  }
}
