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

import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.SourceName;

@EqualsAndHashCode
@ToString
public abstract class Dataset {
  @Getter private final DatasetName name;
  @Getter private final DatasetName physicalName;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter private final SourceName sourceName;
  @Nullable private final String description;

  public Dataset(
      @NonNull final DatasetName name,
      @NonNull final DatasetName physicalName,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @NonNull final SourceName sourceName,
      @Nullable final String description) {
    this.name = name;
    this.physicalName = physicalName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.sourceName = sourceName;
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
