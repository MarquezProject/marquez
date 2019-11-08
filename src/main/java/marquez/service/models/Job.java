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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.JobType;

@Value
public class Job {
  @NonNull JobType type;
  @NonNull JobName name;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull List<DatasetName> inputs;
  @NonNull List<DatasetName> outputs;
  @Nullable URL location;
  @Nullable Map<String, String> context;
  @Nullable String description;

  public List<DatasetName> getInputs() {
    return ImmutableList.copyOf(new ArrayList<>(inputs));
  }

  public List<DatasetName> getOutputs() {
    return ImmutableList.copyOf(new ArrayList<>(outputs));
  }

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public Map<String, String> getContext() {
    return (context == null) ? ImmutableMap.of() : ImmutableMap.copyOf(context);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
