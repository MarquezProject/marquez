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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public final class DbTableVersionRequest extends DatasetVersionRequest {
  @Getter private final String connectionUrl;
  @Getter private final String schema;
  @Getter private final String table;

  public DbTableVersionRequest(
      @NonNull final DatasetType type,
      @NonNull final String connectionUrl,
      @NonNull final String schema,
      @NonNull final String table,
      @NonNull final String description) {
    super(type, description);
    this.connectionUrl = connectionUrl;
    this.schema = schema;
    this.table = table;
  }
}
