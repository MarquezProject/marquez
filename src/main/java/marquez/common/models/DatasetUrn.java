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

import static marquez.common.models.UrnPattern.URN_DELIM;
import static marquez.common.models.UrnPattern.URN_PREFIX;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.StringJoiner;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class DatasetUrn {
  private static final UrnPattern URN_PATTERN = UrnPattern.from(UrnType.DATASET);

  @Getter private final String value;

  private DatasetUrn(@NonNull final String value) {
    URN_PATTERN.throwIfNoMatch(value);
    this.value = value;
  }

  public static DatasetUrn from(
      @NonNull DatasourceName datasourceName, @NonNull DatasetName datasetName) {
    final String value =
        new StringJoiner(URN_DELIM)
            .add(URN_PREFIX)
            .add(UrnType.DATASOURCE.toString())
            .add(datasourceName.getValue())
            .add(datasetName.getValue())
            .toString();
    return fromString(value);
  }

  @JsonProperty
  public static DatasetUrn fromString(String value) {
    return new DatasetUrn(value);
  }
}
