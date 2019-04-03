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

import java.util.Random;

public final class CommonModelGenerator {
  private CommonModelGenerator() {}

  private static final int BOUND = DatasourceType.values().length;
  private static final Random RANDOM = new Random();

  public static OwnerName newOwnerName() {
    return newOwnerNameWith("test_owner" + newId());
  }

  public static OwnerName newOwnerNameWith(String value) {
    return OwnerName.fromString(value);
  }

  public static NamespaceName newNamespaceName() {
    return newNamespaceNameWith("test_namespace" + newId());
  }

  public static NamespaceName newNamespaceNameWith(String value) {
    return NamespaceName.fromString(value);
  }

  public static DatasourceType newDatasourceType() {
    return DatasourceType.values()[newIdWithBound(BOUND)];
  }

  public static DatasourceName newDatasourceName() {
    return newDatasourceNameWith("test_datasource" + newId());
  }

  public static DatasourceName newDatasourceNameWith(String value) {
    return DatasourceName.fromString(value);
  }

  public static DatasourceUrn newDatasourceUrn() {
    return newDatasourceUrnWith(newDatasourceType());
  }

  public static DatasourceUrn newDatasourceUrnWith(DatasourceType type) {
    return DatasourceUrn.from(type, newDatasourceName());
  }

  public static DatasourceUrn newDatasourceUrnWith(String value) {
    return DatasourceUrn.fromString(value);
  }

  public static DbName newDbName() {
    return newDbNameWith("test_db" + newId());
  }

  public static DbName newDbNameWith(String value) {
    return DbName.fromString(value);
  }

  public static ConnectionUrl newConnectionUrl() {
    return newConnectionUrlWith(
        String.format("jdbc:%s://localhost:5432/%s", newDatasourceType(), newDbName().getValue()));
  }

  public static ConnectionUrl newConnectionUrlWith(String value) {
    return ConnectionUrl.fromString(value);
  }

  public static DatasetName newDatasetName() {
    return newDatasetNameWith("test_dataset" + newId());
  }

  public static DatasetName newDatasetNameWith(String value) {
    return DatasetName.fromString(value);
  }

  public static DatasetUrn newDatasetUrn() {
    return DatasetUrn.from(newDatasourceName(), newDatasetName());
  }

  public static DatasetUrn newDatasetUrnWith(String value) {
    return DatasetUrn.fromString(value);
  }

  public static Description newDescription() {
    return newDescriptionWith("test_desciption" + newId());
  }

  public static Description newDescriptionWith(String value) {
    return Description.fromString(value);
  }

  private static int newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  private static int newIdWithBound(int bound) {
    return RANDOM.nextInt(BOUND);
  }
}
