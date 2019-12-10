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

package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.service.models.Tag;

@Value
public class Field {
  String name;
  FieldType type;
  Instant createdAt;
  @Nullable String description;
  @Nullable List<Tag> tags;

  @JsonCreator
  public Field(
      @NonNull final String name,
      @NonNull final FieldType type,
      @Nullable final String description,
      @Nullable final List<Tag> tags,
      final Instant createdAt) {
    this.name = name;
    this.type = type;
    this.description = description;
    this.tags = tags;
    this.createdAt = createdAt;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
