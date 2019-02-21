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

import static marquez.common.Preconditions.checkArgument;
import static marquez.common.Preconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class RunId {
  private static final int ID_LENGTH = 36;

  @Getter private final UUID value;

  public RunId(@NonNull final String value) {
    checkNotBlank(value, "value must not be blank or empty");
    checkArgument(value.length() == ID_LENGTH, String.format("value length must = %d", ID_LENGTH));
    this.value = UUID.fromString(value);
  }

  @JsonCreator
  public static RunId fromString(String value) {
    return new RunId(value);
  }
}
