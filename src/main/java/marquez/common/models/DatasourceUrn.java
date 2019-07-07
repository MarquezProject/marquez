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

import static marquez.common.base.MorePreconditions.checkNotBlank;

import lombok.NonNull;

public final class DatasourceUrn extends Urn {
  private static final String NAMESPACE = "datasource";
  private static final int NUM_OF_PARTS = 2;
  private static final UrnPattern PATTERN = UrnPattern.of(NAMESPACE, NUM_OF_PARTS);

  private DatasourceUrn(final String value) {
    super(checkNotBlank(value));
  }

  public static DatasourceUrn of(
      @NonNull final ConnectionUrl connectionUrl, @NonNull final DatasourceName datasourceName) {
    return of(connectionUrl.getDatasourceType(), datasourceName);
  }

  public static DatasourceUrn of(
      @NonNull final DatasourceType datasourceType, @NonNull final DatasourceName datasourceName) {
    final String value = valueFrom(NAMESPACE, datasourceType.toString(), datasourceName.getValue());
    return of(value);
  }

  public static DatasourceUrn of(final String value) {
    return new DatasourceUrn(value);
  }

  @Override
  public String namespace() {
    return NAMESPACE;
  }

  @Override
  UrnPattern pattern() {
    return PATTERN;
  }
}
