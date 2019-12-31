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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.Field;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonPropertyOrder({
  "type",
  "name",
  "physicalName",
  "createdAt",
  "updatedAt",
  "sourceName",
  "schemaLocation",
  "fields",
  "tags",
  "lastModified",
  "description"
})
public final class StreamResponse extends DatasetResponse {
  @Getter private final String schemaLocation;

  public StreamResponse(
      final String name,
      final String physicalName,
      final String createdAt,
      final String updatedAt,
      final String sourceName,
      @NonNull final String schemaLocation,
      @Nullable final List<Field> fields,
      @Nullable final List<String> tags,
      @Nullable final String lastModified,
      @Nullable final String description) {
    super(
        name,
        physicalName,
        createdAt,
        updatedAt,
        sourceName,
        fields,
        tags,
        lastModified,
        description);
    this.schemaLocation = checkNotBlank(schemaLocation);
  }
}
