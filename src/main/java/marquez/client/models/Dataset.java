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

package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@EqualsAndHashCode
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTable.class, name = "DB_TABLE"),
  @JsonSubTypes.Type(value = Stream.class, name = "STREAM")
})
public abstract class Dataset {
  @Getter @NonNull private final String name;
  @Getter @NonNull private final String physicalName;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  @Getter @NonNull private final String sourceName;
  @Nullable private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public static Dataset fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<Dataset>() {});
  }
}
