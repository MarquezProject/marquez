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

package marquez.common.models;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import marquez.ModelGenerator;

public final class CommonModelGenerator extends ModelGenerator {
  private CommonModelGenerator() {}

  public static OwnerName newOwnerName() {
    return OwnerName.of("test_owner" + newId());
  }

  public static NamespaceName newNamespaceName() {
    return NamespaceName.of("test_namespace" + newId());
  }

  public static JobName newJobName() {
    return JobName.of("test_job" + newId());
  }

  public static URI newLocation() {
    return URI.create("https://github.com/repo/test/commit/" + newId());
  }

  public static DatasourceType newDatasourceType() {
    return DatasourceType.values()[newIdWithBound(DatasourceType.values().length - 1)];
  }

  public static DatasourceName newDatasourceName() {
    return DatasourceName.of("test_datasource" + newId());
  }

  public static DatasourceUrn newDatasourceUrn() {
    return newDatasourceUrnWith(newDatasourceType());
  }

  public static DatasourceUrn newDatasourceUrnWith(final DatasourceType type) {
    return DatasourceUrn.of(type, newDatasourceName());
  }

  public static DbName newDbName() {
    return DbName.of("test_db" + newId());
  }

  public static ConnectionUrl newConnectionUrl() {
    return newConnectionUrlWith(newDatasourceType());
  }

  public static ConnectionUrl newConnectionUrlWith(final DatasourceType type) {
    return ConnectionUrl.of(
        String.format("jdbc:%s://localhost:5432/%s", type.toString(), newDbName().getValue()));
  }

  public static DatasetName newDatasetName() {
    return DatasetName.of("test_dataset" + newId());
  }

  public static List<DatasetUrn> newDatasetUrns(final int limit) {
    return Stream.generate(() -> newDatasetUrn()).limit(limit).collect(toList());
  }

  public static DatasetUrn newDatasetUrn() {
    return DatasetUrn.of(newDatasourceName(), newDatasetName());
  }

  public static Description newDescription() {
    return Description.of("test_description" + newId());
  }
}
