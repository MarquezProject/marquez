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
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetId;

@Value
public class JobRow {
  @NonNull UUID uuid;
  @NonNull String type;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull String namespaceName;
  @NonNull String name;
  @Nullable String description;
  @Nullable UUID currentVersionUuid;
  @Nullable UUID jobContextUuid;
  @Nullable String location;
  @Nullable Set<DatasetId> inputs;
  @Nullable Set<DatasetId> outputs;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<UUID> getCurrentVersionUuid() {
    return Optional.ofNullable(currentVersionUuid);
  }
}
