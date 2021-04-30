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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;
import marquez.common.models.Version;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableVersion extends DatasetVersion {
  public DbTableVersion(
      final DatasetId id,
      final DatasetName name,
      final DatasetName physicalName,
      final Instant createdAt,
      final Version version,
      final SourceName sourceName,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String description,
      @Nullable final Run createdByRun,
      @Nullable final ImmutableMap<String, Object> facets) {
    super(
        id,
        DB_TABLE,
        name,
        physicalName,
        createdAt,
        version,
        sourceName,
        fields,
        tags,
        description,
        createdByRun,
        facets);
  }
}
