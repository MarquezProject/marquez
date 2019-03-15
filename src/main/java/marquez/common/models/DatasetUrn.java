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

public final class DatasetUrn extends Urn {
  private static final int NUM_OF_PARTS = 2;
  private static final String URN_TYPE = "dataset";
  private static final UrnPattern URN_PATTERN = UrnPattern.from(URN_TYPE, NUM_OF_PARTS);

  private DatasetUrn(@NonNull final String value) {
    super(checkNotBlank(value));
  }

  @Deprecated
  public static DatasetUrn from(
      @NonNull NamespaceName namespaceName, @NonNull DatasetName datasetName) {
    final String value =
        fromTypeAndParts(URN_TYPE, namespaceName.toString(), datasetName.getValue());
    return fromString(value);
  }

  public static DatasetUrn from(
      @NonNull DatasourceName datasourceName, @NonNull DatasetName datasetName) {
    final String value =
        fromTypeAndParts(URN_TYPE, datasourceName.toString(), datasetName.getValue());
    return fromString(value);
  }

  @JsonCreator
  public static DatasetUrn fromString(String value) {
    return new DatasetUrn(value);
  }

  @Override
  UrnPattern pattern() {
    return URN_PATTERN;
  }
}
