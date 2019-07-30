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

import static marquez.common.base.MorePreconditions.checkNotBlank;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class JobRunResponse {
  @Getter private final String runId;
  private final String nominalStartTime;
  private final String nominalEndTime;
  private final String runArgs;
  @Getter private final String runState;

  public JobRunResponse(
      @NonNull final String runId,
      @Nullable final String nominalStartTime,
      @Nullable final String nominalEndTime,
      @Nullable final String runArgs,
      @NonNull final String runState) {
    this.runId = checkNotBlank(runId);
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.runArgs = runArgs;
    this.runState = checkNotBlank(runState);
  }

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
