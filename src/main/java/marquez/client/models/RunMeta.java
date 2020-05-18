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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.client.Utils;

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
public class RunMeta {
  @Nullable private final Instant nominalStartTime;
  @Nullable private final Instant nominalEndTime;

  @Getter
  @JsonProperty("runArgs")
  private final Map<String, String> args;

  public RunMeta(
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @JsonProperty("runArgs") @Nullable final Map<String, String> args) {
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.args = (args == null) ? ImmutableMap.of() : ImmutableMap.copyOf(args);
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public String toJson() {
    return Utils.toJson(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    @Nullable private Instant nominalStartTime;
    @Nullable private Instant nominalEndTime;
    @Nullable private Map<String, String> args;

    private Builder() {
      this.args = ImmutableMap.of();
    }

    public Builder nominalStartTime(@Nullable Instant nominalStartTime) {
      this.nominalStartTime = nominalStartTime;
      return this;
    }

    public Builder nominalEndTime(@Nullable Instant nominalEndTime) {
      this.nominalEndTime = nominalEndTime;
      return this;
    }

    public Builder args(@Nullable Map<String, String> args) {
      this.args = (args == null) ? ImmutableMap.of() : ImmutableMap.copyOf(args);
      return this;
    }

    public RunMeta build() {
      return new RunMeta(nominalStartTime, nominalEndTime, args);
    }
  }
}
