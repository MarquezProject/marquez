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
import java.util.Random;
import java.util.stream.Stream;

public final class CommonModelGenerator {
  private CommonModelGenerator() {}

  private static final int BOUND = DatasourceType.values().length;
  private static final Random RANDOM = new Random();

  public static OwnerName newOwnerName() {
    return newOwnerNameWith("test_owner" + newId());
  }

  public static OwnerName newOwnerNameWith(String value) {
    return OwnerName.of(value);
  }

  public static NamespaceName newNamespaceName() {
    return newNamespaceNameWith("test_namespace" + newId());
  }

  public static NamespaceName newNamespaceNameWith(String value) {
    return NamespaceName.of(value);
  }

  public static JobName newJobName() {
    return newJobNameWith("test_job" + newId());
  }

  public static JobName newJobNameWith(String value) {
    return JobName.of(value);
  }

  public static URI newLocation() {
    return newLocationWith("https://github.com/repo/test/commit/" + newId());
  }

  public static URI newLocationWith(String value) {
    return URI.create(value);
  }

  public static DatasourceType newDatasourceType() {
    return DatasourceType.values()[newIdWithBound(BOUND)];
  }

  public static DatasourceName newDatasourceName() {
    return newDatasourceNameWith("test_datasource" + newId());
  }

  public static DatasourceName newDatasourceNameWith(String value) {
    return DatasourceName.of(value);
  }

  public static DatasourceUrn newDatasourceUrn() {
    return newDatasourceUrnWith(newDatasourceType());
  }

  public static DatasourceUrn newDatasourceUrnWith(DatasourceType type) {
    return DatasourceUrn.of(type, newDatasourceName());
  }

  public static DatasourceUrn newDatasourceUrnWith(String value) {
    return DatasourceUrn.of(value);
  }

  public static DbName newDbName() {
    return newDbNameWith("test_db" + newId());
  }

  public static DbName newDbNameWith(String value) {
    return DbName.of(value);
  }

  public static ConnectionUrl newConnectionUrl() {
    return newConnectionUrlWith(newDatasourceType());
  }

  public static ConnectionUrl newConnectionUrlWith(DatasourceType datasourceType) {
    return newConnectionUrlWith(
        String.format("jdbc:%s://localhost:5432/%s", datasourceType, newDbName().getValue()));
  }

  public static ConnectionUrl newConnectionUrlWith(String value) {
    return ConnectionUrl.of(value);
  }

  public static DatasetName newDatasetName() {
    return newDatasetNameWith("test_dataset" + newId());
  }

  public static DatasetName newDatasetNameWith(String value) {
    return DatasetName.of(value);
  }

  public static List<DatasetUrn> newDatasetUrns(int limit) {
    return Stream.generate(() -> newDatasetUrn()).limit(limit).collect(toList());
  }

  public static DatasetUrn newDatasetUrn() {
    return DatasetUrn.of(newDatasourceName(), newDatasetName());
  }

  public static Description newDescription() {
    return newDescriptionWith("test_description" + newId());
  }

  public static Description newDescriptionWith(String value) {
    return Description.of(value);
  }

  private static int newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  private static int newIdWithBound(int bound) {
    return RANDOM.nextInt(BOUND);
  }
}
