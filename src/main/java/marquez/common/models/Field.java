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

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class Field {
  @JsonUnwrapped
  @JsonProperty(access = READ_ONLY)
  FieldName name;

  FieldType type;
  @Nullable List<String> tags;
  @Nullable String description;

  @JsonCreator
  public Field(
      @JsonProperty("name") final String nameAsString,
      @JsonProperty("type") final String typeAsString,
      final List<String> tags,
      final String description) {
    this(FieldName.fromString(nameAsString), FieldType.valueOf(typeAsString), tags, description);
  }

  public Field(
      @NonNull final FieldName name,
      @NonNull final FieldType type,
      @Nullable final List<String> tags,
      @Nullable final String description) {
    this.name = name;
    this.type = type;
    this.tags = (tags == null) ? ImmutableList.of() : ImmutableList.copyOf(tags);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
