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

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.StringJoiner;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class DatasourceUrn {
  private static final UrnPattern URN_PATTERN = UrnPattern.from(UrnType.DATASOURCE);

  @Getter private final String value;

  private DatasourceUrn(@NonNull final String value) {
    URN_PATTERN.throwIfNoMatch(value);
    this.value = value;
  }

  public static DatasourceUrn from(@NonNull DatasourceType type, @NonNull DatasourceName name) {
    final String value =
        new StringJoiner(URN_DELIM)
            .add(URN_PREFIX)
            .add(UrnType.DATASOURCE.toString())
            .add(type.toString())
            .add(name.getValue())
            .toString();
    return fromString(value);
  }

  @JsonCreator
  public static DatasourceUrn fromString(String value) {
    return new DatasourceUrn(value);
  }
}
