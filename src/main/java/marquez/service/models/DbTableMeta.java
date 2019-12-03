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

import static com.google.common.base.Charsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static marquez.common.Utils.VERSION_DELIM;
import static marquez.common.Utils.VERSION_JOINER;
import static marquez.common.models.DatasetType.DB_TABLE;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableMeta extends DatasetMeta {
  public DbTableMeta(
      final DatasetName physicalName,
      final SourceName sourceName,
      @Nullable final List<Field> fields,
      @Nullable final String description,
      @Nullable final UUID runId) {
    super(DB_TABLE, physicalName, sourceName, fields, description, runId);
  }

  @Override
  public UUID version(@NonNull NamespaceName namespaceName, @NonNull DatasetName datasetName) {
    final byte[] bytes =
        VERSION_JOINER
            .join(
                namespaceName.getValue(),
                getSourceName().getValue(),
                datasetName.getValue(),
                getPhysicalName().getValue(),
                getFields().stream().map(DatasetMeta::joinField).collect(joining(VERSION_DELIM)))
            .getBytes(UTF_8);
    return UUID.nameUUIDFromBytes(bytes);
  }
}
