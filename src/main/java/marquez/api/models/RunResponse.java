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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonPropertyOrder({
  "runId",
  "createdAt",
  "updatedAt",
  "nominalStartTime",
  "nominalEndTime",
  "runState",
  "runArgs"
})
public final class RunResponse {
  @Getter
  @JsonProperty("runId")
  private final String id;

  @Getter private final String createdAt;
  @Getter private final String updatedAt;

  @Nullable private final String nominalStartTime;
  @Nullable private final String nominalEndTime;

  @Getter
  @JsonProperty("runState")
  private final String state;

  @Getter
  @Nullable
  @JsonProperty("runArgs")
  private final Map<String, String> args;

  public RunResponse(
      @JsonProperty("runId") @NonNull final String id,
      @NonNull final String createdAt,
      @NonNull final String updatedAt,
      @Nullable final String nominalStartTime,
      @Nullable final String nominalEndTime,
      @JsonProperty("runState") @NonNull final String state,
      @JsonProperty("runArgs") @Nullable final Map<String, String> args) {
    this.id = checkNotBlank(id);
    this.createdAt = checkNotBlank(createdAt);
    this.updatedAt = checkNotBlank(updatedAt);
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.state = checkNotBlank(state);
    this.args = (args == null) ? ImmutableMap.of() : ImmutableMap.copyOf(args);
  }

  public Optional<String> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<String> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }
}
