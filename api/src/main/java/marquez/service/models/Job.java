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

package marquez.service.models;

import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;

@EqualsAndHashCode
@ToString
public final class Job {
  @Getter private final JobId id;
  @Getter private final JobType type;
  @Getter private final JobName name;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter private final NamespaceName namespace;
  @Getter private final Set<DatasetId> inputs;
  @Getter private final Set<DatasetId> outputs;
  @Nullable private final URL location;
  @Getter private final ImmutableMap<String, String> context;
  @Nullable private final String description;
  @Nullable private final Run latestRun;

  public Job(
      @NonNull final JobId id,
      @NonNull final JobType type,
      @NonNull final JobName name,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @NonNull final Set<DatasetId> inputs,
      @NonNull final Set<DatasetId> outputs,
      @Nullable final URL location,
      @Nullable final ImmutableMap<String, String> context,
      @Nullable final String description,
      @Nullable final Run latestRun) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.namespace = id.getNamespace();
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : context;
    this.description = description;
    this.latestRun = latestRun;
  }

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<Run> getLatestRun() {
    return Optional.ofNullable(latestRun);
  }
}
