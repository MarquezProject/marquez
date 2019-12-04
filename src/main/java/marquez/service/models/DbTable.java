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

import static marquez.common.models.DatasetType.DB_TABLE;

import java.time.Instant;
import java.util.List;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.SourceName;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTable extends Dataset {
  public DbTable(
      final DatasetName name,
      final DatasetName physicalName,
      final Instant createdAt,
      final Instant updatedAt,
      final SourceName sourceName,
      @Nullable final List<Field> fields,
      @Nullable final Instant lastModified,
      @Nullable final String description) {
    super(
        DB_TABLE,
        name,
        physicalName,
        createdAt,
        updatedAt,
        sourceName,
        fields,
        lastModified,
        description);
  }
}
