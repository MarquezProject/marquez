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

package marquez.api.models;

import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTableResponse.class, name = "DB_TABLE"),
  @JsonSubTypes.Type(value = StreamResponse.class, name = "STREAM")
})
public abstract class DatasetResponse {
  @Getter private final String name;
  @Getter private final String physicalName;
  @Getter private final String createdAt;
  @Getter private final String updatedAt;
  @Getter private final String sourceName;
  @Nullable private final String description;

  public DatasetResponse(
      @NonNull final String name,
      @NonNull final String physicalName,
      @NonNull final String createdAt,
      @NonNull final String updatedAt,
      @NonNull final String sourceName,
      @Nullable final String description) {
    this.name = checkNotBlank(name);
    this.physicalName = checkNotBlank(physicalName);
    this.createdAt = checkNotBlank(createdAt);
    this.updatedAt = checkNotBlank(updatedAt);
    this.sourceName = checkNotBlank(sourceName);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
