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
import static marquez.common.models.UrnPattern.URN_DELIM;
import static marquez.common.models.UrnPattern.URN_PREFIX;

import java.util.StringJoiner;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Urn {
  @Getter private final String value;

  protected Urn(@NonNull final String value) {
    this.value = checkNotBlank(value);
  }

  protected static String fromParts(@NonNull String... parts) {
    return new StringJoiner(URN_DELIM)
        .add(URN_PREFIX)
        .add(String.join(URN_DELIM, parts))
        .toString();
  }
}
