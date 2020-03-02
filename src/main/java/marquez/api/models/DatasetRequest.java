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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.common.models.Field;

@EqualsAndHashCode
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTableRequest.class, name = "DB_TABLE"),
  @JsonSubTypes.Type(value = StreamRequest.class, name = "STREAM")
})
public abstract class DatasetRequest {
  @Getter @NotEmpty private final String physicalName;
  @Getter @NotEmpty private final String sourceName;
  @Getter private final List<Field> fields;
  @Getter private final List<String> tags;
  @Nullable private final String description;
  @Nullable private final String runId;

  @JsonCreator
  public DatasetRequest(
      final String physicalName,
      final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final List<String> tags,
      @Nullable final String description,
      @Nullable final String runId) {
    this.physicalName = physicalName;
    this.sourceName = sourceName;
    this.fields = (fields == null) ? ImmutableList.of() : ImmutableList.copyOf(fields);
    this.tags = (tags == null) ? ImmutableList.of() : ImmutableList.copyOf(tags);
    this.description = description;
    this.runId = runId;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<String> getRunId() {
    return Optional.ofNullable(runId);
  }
}
