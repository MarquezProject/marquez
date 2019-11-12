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

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.DbColumn;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableMeta extends DatasetMeta {
  final List<DbColumn> columns;

  public DbTableMeta(
      final DatasetName physicalName,
      final SourceName sourceName,
      @Nullable final String description,
      @Nullable final UUID runId,
      @Nullable final List<DbColumn> columns) {
    super(physicalName, sourceName, description, runId);
    this.columns = columns;
  }

  @Override
  public UUID version(NamespaceName namespaceName, DatasetName datasetName) {
    return null; // todo: implement version logic
  }
}
