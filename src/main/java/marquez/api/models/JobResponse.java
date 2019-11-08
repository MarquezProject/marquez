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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class JobResponse {
  @Getter private final String type;
  @Getter private final String name;
  @Getter private final String createdAt;
  @Getter private final String updatedAt;
  private final List<String> inputs;
  private final List<String> outputs;
  @Nullable private final String location;
  @Nullable private final Map<String, String> context;
  @Nullable private final String description;

  public JobResponse(
      @NonNull final String type,
      @NonNull final String name,
      @NonNull final String createdAt,
      @NonNull final String updatedAt,
      @NonNull final List<String> inputs,
      @NonNull final List<String> outputs,
      @Nullable final String location,
      @Nullable final Map<String, String> context,
      @Nullable final String description) {
    this.type = checkNotBlank(type);
    this.name = checkNotBlank(name);
    this.createdAt = checkNotBlank(createdAt);
    this.updatedAt = checkNotBlank(updatedAt);
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.context = context;
    this.description = description;
  }

  public List<String> getInputs() {
    return ImmutableList.copyOf(new ArrayList<>(inputs));
  }

  public List<String> getOutputs() {
    return ImmutableList.copyOf(new ArrayList<>(outputs));
  }

  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
  }

  public Map<String, String> getContext() {
    return (context == null) ? ImmutableMap.of() : ImmutableMap.copyOf(context);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
