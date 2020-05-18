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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public class JobMeta {
  @Getter @NonNull JobType type;
  @Getter @NonNull Set<DatasetId> inputs;
  @Getter @NonNull Set<DatasetId> outputs;
  @Getter @NonNull URL location;
  @Nullable String description;
  @Getter @NonNull Map<String, String> context;

  public JobMeta(
      @NonNull final JobType type,
      @NonNull final Set<DatasetId> inputs,
      @NonNull final Set<DatasetId> outputs,
      @NonNull final URL location,
      @Nullable final String description,
      @Nullable final Map<String, String> context) {
    this.type = type;
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.description = description;
    this.context = (context == null) ? ImmutableMap.of() : context;
  }

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
    private Set<DatasetId> inputs;
    private Set<DatasetId> outputs;
    private URL location;
    @Nullable private String description;
    @Nullable Map<String, String> context;

    private Builder() {
      this.inputs = ImmutableSet.of();
      this.outputs = ImmutableSet.of();
    }

    public Builder type(@NonNull String typeString) {
      return type(JobType.valueOf(typeString));
    }

    public Builder type(@NonNull JobType type) {
      this.type = type;
      return this;
    }

    public Builder inputs(@NonNull Set<DatasetId> inputs) {
      this.inputs = ImmutableSet.copyOf(inputs);
      return this;
    }

    public Builder outputs(@NonNull Set<DatasetId> outputs) {
      this.outputs = ImmutableSet.copyOf(outputs);
      return this;
    }

    public Builder location(@NonNull URL location) {
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
