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
public final class DatasetRow {
  @Getter private final UUID uuid;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter @NonNull private final UUID namespaceUuid;
  @Getter @NonNull private final UUID dataSourceUuid;
  @Getter @NonNull private final String urn;
  @Getter private final UUID currentVersion;
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
