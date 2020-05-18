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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

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
  @Nullable private final List<Field> fields;
  @Nullable private final List<String> tags;
  @Nullable private final Instant lastModifiedAt;
  @Nullable private final String description;

  public Dataset(
      @NonNull String name,
      @NonNull String physicalName,
      @NonNull Instant createdAt,
      @NonNull Instant updatedAt,
      @NonNull String sourceName,
      @Nullable List<Field> fields,
      @Nullable List<String> tags,
      @Nullable Instant lastModifiedAt,
      @Nullable String description) {
    this.name = name;
    this.physicalName = physicalName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.sourceName = sourceName;
    this.fields = ImmutableList.copyOf(new ArrayList<>(fields));
    this.tags = ImmutableList.copyOf(new ArrayList<>(tags));
    this.lastModifiedAt = lastModifiedAt;
    this.description = description;
  }

  public List<Field> getFields() {
    return fields;
  }

  public List<String> getTags() {
    return tags;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<Instant> getLastModifiedAt() {
    return Optional.ofNullable(lastModifiedAt);
  }

  public static Dataset fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<Dataset>() {});
  }
}
