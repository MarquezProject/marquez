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

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import marquez.client.Utils;

@Value
public class JobMeta {
  @Getter @NonNull JobType type;
  @Getter @NonNull List<String> inputs;
  @Getter @NonNull List<String> outputs;
  @Getter @NonNull String location;
  @Nullable String description;
  @Nullable Map<String, String> context;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public String toJson() {
    return Utils.toJson(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private JobType type;
    private List<String> inputs;
    private List<String> outputs;
    private String location;
    @Nullable private String description;
    @Nullable Map<String, String> context;

    private Builder() {
      this.inputs = ImmutableList.of();
      this.outputs = ImmutableList.of();
    }

    public Builder type(@NonNull String typeString) {
      return type(JobType.valueOf(typeString));
    }

    public Builder type(@NonNull JobType type) {
      this.type = type;
      return this;
    }

    public Builder inputs(@NonNull List<String> inputs) {
      this.inputs = ImmutableList.copyOf(inputs);
      return this;
    }

    public Builder outputs(@NonNull List<String> outputs) {
      this.outputs = ImmutableList.copyOf(outputs);
      return this;
    }

    public Builder location(@NonNull String location) {
      this.location = location;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public Builder context(@Nullable Map<String, String> context) {
      this.context = context;
      return this;
    }

    public JobMeta build() {
      return new JobMeta(type, inputs, outputs, location, description, context);
    }
  }
}
