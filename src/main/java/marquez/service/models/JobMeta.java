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

import static com.google.common.base.Charsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static marquez.common.Utils.KV_JOINER;
import static marquez.common.Utils.VERSION_DELIM;
import static marquez.common.Utils.VERSION_JOINER;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;

@EqualsAndHashCode
@ToString
public final class JobMeta {
  @Getter private final JobType type;
  @Getter private final ImmutableSet<DatasetId> inputs;
  @Getter private final ImmutableSet<DatasetId> outputs;
  @Nullable private final URL location;
  @Getter private final ImmutableMap<String, String> context;
  @Nullable private final String description;

  public JobMeta(
      @NonNull final JobType type,
      @NonNull final ImmutableSet<DatasetId> inputs,
      @NonNull final ImmutableSet<DatasetId> outputs,
      @Nullable final URL location,
      @Nullable final ImmutableMap<String, String> context,
      @Nullable final String description) {
    this.type = type;
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : context;
    this.description = description;
  }

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Version version(@NonNull NamespaceName namespaceName, @NonNull JobName jobName) {
    final byte[] bytes =
        VERSION_JOINER
            .join(
                namespaceName.getValue(),
                jobName.getValue(),
                getInputs().stream().flatMap(JobMeta::idToStream).collect(joining(VERSION_DELIM)),
                getOutputs().stream().flatMap(JobMeta::idToStream).collect(joining(VERSION_DELIM)),
                getLocation().map(URL::toString).orElse(null),
                KV_JOINER.join(getContext()))
            .getBytes(UTF_8);
    return Version.of(UUID.nameUUIDFromBytes(bytes));
  }

  private static java.util.stream.Stream<String> idToStream(DatasetId datasetId) {
    return Stream.of(datasetId.getNamespace().getValue(), datasetId.getName().getValue());
  }
}
