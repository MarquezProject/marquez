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
public class JobVersionRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updateAt;
  @Getter @NonNull private final UUID jobUuid;
  @Getter @NonNull private final UUID jobContextUuid;
  @Getter @NonNull private final List<UUID> inputs;
  @Getter @NonNull private final List<UUID> outputs;
  @Nullable private final String location;
  @Getter @NonNull private final UUID version;
  @Nullable private final UUID latestRunUuid;

  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<UUID> getLatestRunUuid() {
    return Optional.ofNullable(latestRunUuid);
  }
}
