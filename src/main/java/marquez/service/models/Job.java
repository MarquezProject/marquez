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

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
public final class Job {
  @NonNull @Getter private final UUID guid;
  @NonNull @Getter private final String name;
  @NonNull @Getter private final String location;
  @NonNull @Getter @Setter private UUID namespaceGuid;
  @Getter private final String description;
  @Getter private final List<String> inputDatasetUrns;
  @Getter private final List<String> outputDatasetUrns;
  @Getter private final Instant createdAt;

  public Job(
      final UUID guid,
      final String name,
      final String location,
      final UUID namespaceGuid,
      final String description,
      final List<String> inputDatasetUrns,
      final List<String> outputDatasetUrns) {
    this.guid = guid;
    this.name = name;
    this.location = location;
    this.namespaceGuid = namespaceGuid;
    this.description = description;
    this.inputDatasetUrns = inputDatasetUrns;
    this.outputDatasetUrns = outputDatasetUrns;
    this.createdAt = null;
  }
}
