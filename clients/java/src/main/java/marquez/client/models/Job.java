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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Job extends JobMeta {
  @Getter private final JobId id;
  @Getter private final String name;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter private final String namespace;
  @Nullable private final Run latestRun;
  @Getter private final Map<String, Object> facets;

  public Job(
      @NonNull final JobId id,
      final JobType type,
      @NonNull final String name,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @NonNull final String namespace,
      final Set<DatasetId> inputs,
      final Set<DatasetId> outputs,
      @Nullable final URL location,
      final Map<String, String> context,
      final String description,
      @Nullable final Run latestRun,
      @Nullable final Map<String, Object> facets) {
    super(type, inputs, outputs, location, context, description, null);
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.namespace = namespace;
    this.latestRun = latestRun;
    this.facets = (facets == null) ? ImmutableMap.of() : ImmutableMap.copyOf(facets);
  }

  public Optional<Run> getLatestRun() {
    return Optional.ofNullable(latestRun);
  }

  public boolean hasFacets() {
    return !facets.isEmpty();
  }

  public static Job fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<Job>() {});
  }
}
