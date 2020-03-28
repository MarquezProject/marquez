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

import static java.util.stream.Collectors.toUnmodifiableList;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.service.models.DatasetId;
import marquez.service.models.JobId;

@EqualsAndHashCode
@ToString
public final class JobResponse {
  @Getter private final JobId id;
  @Getter private final String type;
  @Getter private final String name;
  @Getter private final String createdAt;
  @Getter private final String updatedAt;
  @Deprecated @Getter private final List<String> inputs;
  @Deprecated @Getter private final List<String> outputs;
  @Getter private final List<DatasetId> inputIds;
  @Getter private final List<DatasetId> outputIds;
  @Nullable private final String location;
  @Getter @Nullable private final Map<String, String> context;
  @Nullable private final String description;
  @Nullable private final RunResponse latestRun;

  public JobResponse(
      @NonNull final JobId id,
      @NonNull final String type,
      @NonNull final String name,
      @NonNull final String createdAt,
      @NonNull final String updatedAt,
      @NonNull final List<DatasetId> inputIds,
      @NonNull final List<DatasetId> outputIds,
      @Nullable final String location,
      @Nullable final Map<String, String> context,
      @Nullable final String description,
      @Nullable final RunResponse latestRun) {
    this.id = id;
    this.type = checkNotBlank(type);
    this.name = checkNotBlank(name);
    this.createdAt = checkNotBlank(createdAt);
    this.updatedAt = checkNotBlank(updatedAt);
    this.inputIds = ImmutableList.copyOf(inputIds);
    this.outputIds = ImmutableList.copyOf(outputIds);
    this.inputs =
        inputIds.stream().map((i) -> i.getName().getValue()).collect(toUnmodifiableList());
    this.outputs =
        outputIds.stream().map((i) -> i.getName().getValue()).collect(toUnmodifiableList());
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : ImmutableMap.copyOf(context);
    this.description = description;
    this.latestRun = latestRun;
  }

  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<RunResponse> getLatestRun() {
    return Optional.ofNullable(latestRun);
  }
}
