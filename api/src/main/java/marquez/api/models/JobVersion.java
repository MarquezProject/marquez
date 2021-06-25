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

import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.Version;
import marquez.service.models.Run;

/**
 * Models a single version of a {@link marquez.service.models.Job}. Optionally includes the latest
 * {@link Run} for the version of the {@link marquez.service.models.Job}.
 */
@EqualsAndHashCode
@ToString
public final class JobVersion {
  @Getter private final JobVersionId id;
  @Getter private final JobName name;
  @Getter private final Instant createdAt;
  @Getter private final Version version;
  @Getter private final NamespaceName namespace;
  @Nullable private final URL location;
  @Getter private final ImmutableMap<String, String> context;
  @Getter private final List<DatasetId> inputs;
  @Getter private final List<DatasetId> outputs;
  @Getter @Nullable private final Run latestRun;

  public JobVersion(
      @NonNull final JobVersionId id,
      @NonNull final JobName name,
      @NonNull final Instant createdAt,
      @NonNull final Version version,
      @Nullable final URL location,
      @Nullable final ImmutableMap<String, String> context,
      List<DatasetId> inputs,
      List<DatasetId> outputs,
      @Nullable Run latestRun) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.version = version;
    this.namespace = id.getNamespace();
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : context;
    this.inputs = inputs;
    this.outputs = outputs;
    this.latestRun = latestRun;
  }

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }
}
