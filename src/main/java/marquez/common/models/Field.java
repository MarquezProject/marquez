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

import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class Field {
  String name;
  FieldType type;
  @Nullable List<String> tags;
  @Nullable String description;

  @JsonCreator
  public Field(
      @NonNull final String name,
      @NonNull final FieldType type,
      @Nullable final List<String> tags,
      @Nullable final String description) {
    this.name = checkNotBlank(name);
    this.type = type;
    this.tags = tags;
    this.description = description;
  }

  public List<String> getTags() {
    return (tags == null) ? ImmutableList.of() : ImmutableList.copyOf(tags);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
