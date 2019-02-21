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

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;

@EqualsAndHashCode
@ToString
@Builder
public final class DbTableVersion implements DatasetVersion {
  @Getter private final ConnectionUrl connectionUrl;
  @Getter private final DbSchemaName dbSchemaName;
  @Getter private final DbTableName dbTableName;
  private final Description description;

  public DbTableVersion(
      @NonNull final ConnectionUrl connectionUrl,
      @NonNull final DbSchemaName dbSchemaName,
      @NonNull final DbTableName dbTableName,
      @Nullable final Description description) {
    this.connectionUrl = connectionUrl;
    this.dbSchemaName = dbSchemaName;
    this.dbTableName = dbTableName;
    this.description = description;
  }

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }

  public String getQualifiedName() {
    return dbSchemaName.getValue() + '.' + dbTableName.getValue();
  }

  @Override
  public DatasetUrn toDatasetUrn(@NonNull NamespaceName namespaceName) {
    return DatasetUrn.from(namespaceName, DatasetName.fromString(getQualifiedName()));
  }
}
