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

import static marquez.client.models.DatasetType.DB_TABLE;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTable extends Dataset {
  public DbTable(
      final DatasetId id,
      final String name,
      final String physicalName,
      final Instant createdAt,
      final Instant updatedAt,
      final String namespace,
      final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final Set<String> tags,
      @Nullable final Instant lastModifiedAt,
      @Nullable final String description,
      @Nullable final Map<String, Object> facets) {
    super(
        id,
        DB_TABLE,
        name,
        physicalName,
        createdAt,
        updatedAt,
        namespace,
        sourceName,
        fields,
        tags,
        lastModifiedAt,
        description,
        facets);
  }
}
