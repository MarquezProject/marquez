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

import static marquez.common.Preconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.NonNull;

public final class DatasourceUrn extends Urn {
  private static final String NAMESPACE = "datasource";
  private static final int NUM_OF_PARTS = 2;
  private static final UrnPattern PATTERN = UrnPattern.from(NAMESPACE, NUM_OF_PARTS);

  private DatasourceUrn(@NonNull final String value) {
    super(checkNotBlank(value));
  }

  public static DatasourceUrn from(@NonNull DatasourceType type, @NonNull DatasourceName name) {
    final String value = valueFrom(NAMESPACE, type.toString(), name.getValue());
    return fromString(value);
  }

  @JsonCreator
  public static DatasourceUrn fromString(String value) {
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
