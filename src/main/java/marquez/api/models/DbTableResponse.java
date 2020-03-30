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

import java.util.List;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.service.models.DatasetId;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableResponse extends DatasetResponse {
  public DbTableResponse(
      @NonNull final DatasetId id,
      @NonNull final NamespaceName namespace,
      final DatasetName name,
      final String physicalName,
      final String createdAt,
      final String updatedAt,
      final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final List<String> tags,
      @Nullable final String lastModifiedAt,
      @Nullable final String description) {
    super(
        id,
        namespace,
        name,
        physicalName,
        createdAt,
        updatedAt,
        sourceName,
        fields,
        tags,
        lastModifiedAt,
        description);
  }
}
