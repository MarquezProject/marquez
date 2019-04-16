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

package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@EqualsAndHashCode
@ToString
public final class JobRunRequest {
  @Nullable private final String nominalStartTime;
  @Nullable private final String nominalEndTime;
  @Nullable private final String runArgs;

  public Optional<String> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<String> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public Optional<String> getRunArgs() {
    return Optional.ofNullable(runArgs);
  }
}
