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
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.client.Utils;

@Value
public class RunMeta {
  @Nullable Instant nominalStartTime;
  @Nullable Instant nominalEndTime;

  @NonNull
  @JsonProperty("runArgs")
  Map<String, String> args;

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
    private Map<String, String> args;

    private Builder() {
      this.args = ImmutableMap.of();
    }

    public Builder nominalStartTime(@NonNull Instant nominalStartTime) {
      this.nominalStartTime = nominalStartTime;
      return this;
    }

    public Builder nominalEndTime(@NonNull Instant nominalEndTime) {
      this.nominalEndTime = nominalEndTime;
      return this;
    }

    public Builder args(@NonNull Map<String, String> args) {
      this.args = ImmutableMap.copyOf(args);
      return this;
    }

    public RunMeta build() {
      return new RunMeta(nominalStartTime, nominalEndTime, args);
    }
  }
}
