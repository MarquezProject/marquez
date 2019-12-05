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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExtendedJobVersionRow extends JobVersionRow {
  @Getter private final String context;

  public ExtendedJobVersionRow(
      final UUID uuid,
      final Instant createdAt,
      final Instant updatedAt,
      final UUID jobUuid,
      final UUID jobContextUuid,
      final List<UUID> inputUuids,
      final List<UUID> outputUuids,
      final String location,
      final UUID version,
      final UUID latestRunUuid,
      @NonNull final String context) {
    super(
        uuid,
        createdAt,
        updatedAt,
        jobUuid,
        jobContextUuid,
        inputUuids,
        outputUuids,
        location,
        version,
        latestRunUuid);
    this.context = context;
  }
}
