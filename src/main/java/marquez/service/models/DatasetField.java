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
import java.util.List;
import lombok.Builder;
import lombok.Data;
import marquez.common.models.Description;
import marquez.common.models.FieldName;
import marquez.common.models.FieldType;

@Data
@Builder
public final class DatasetField {
  private final FieldName name;
  private final FieldType type;
  private final Instant createdAt;
  private final List<Tag> tags;
  private final Description description;

  public String getCreatedAt() {
    return createdAt.toString();
  }

  public String getName() {
    return name.getValue();
  }

  public String getType() {
    return type.toString();
  }

  public String getDescription() {
    return description.getValue();
  }
}
