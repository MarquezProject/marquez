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

package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Run extends RunMeta {
  @Getter
  @JsonProperty("runId")
  private final String id;

  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;

  @Getter
  @JsonProperty("runState")
  private final RunState state;

  public Run(
      @JsonProperty("runId") @NonNull final String id,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @JsonProperty("runState") @NonNull final RunState state,
      @JsonProperty("runArgs") @Nullable final Map<String, String> args) {
    super(nominalStartTime, nominalEndTime, args);
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.state = state;
  }

  public static Run fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<Run>() {});
  }
}
