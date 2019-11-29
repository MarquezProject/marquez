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

import static marquez.common.Utils.VERSION_JOINER;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;

@EqualsAndHashCode
@ToString
public abstract class DatasetMeta {
  @Getter private final DatasetType type;
  @Getter private final DatasetName physicalName;
  @Getter private final SourceName sourceName;
  @Nullable final List<Field> fields;
  @Nullable private final String description;
  @Nullable private final UUID runId;

  public DatasetMeta(
      @NonNull final DatasetType type,
      @NonNull final DatasetName physicalName,
      @NonNull final SourceName sourceName,
      @Nullable final List<Field> fields,
      @Nullable final String description,
      @Nullable final UUID runId) {
    this.type = type;
    this.physicalName = physicalName;
    this.sourceName = sourceName;
    this.fields = fields;
    this.description = description;
    this.runId = runId;
  }

  public List<Field> getFields() {
    return (fields == null) ? ImmutableList.of() : ImmutableList.copyOf(fields);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<UUID> getRunId() {
    return Optional.ofNullable(runId);
  }

  public abstract UUID version(NamespaceName namespaceName, DatasetName datasetName);

  protected static String joinField(final Field field) {
    return VERSION_JOINER.join(field.getName(), field.getType(), field.getDescription());
  }
}
