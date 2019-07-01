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

import static marquez.common.Preconditions.checkNotBlank;
import static marquez.common.Preconditions.checkNotEmpty;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.*;

@EqualsAndHashCode
@ToString
public final class JobResponse {
  @Getter private String name;
  @Getter private String createdAt;
  @Getter private String updatedAt;
  @Getter private List<String> inputDatasetUrns;
  @Getter private List<String> outputDatasetUrns;
  @Getter private String location;
  private String description;

  public JobResponse(
      @NonNull final String name,
      @NonNull final String createdAt,
      @NonNull final String updatedAt,
      @NonNull final List<String> inputDatasetUrns,
      @NonNull final List<String> outputDatasetUrns,
      @NonNull String location,
      @Nullable String description) {
    this.name = checkNotBlank(name);
    this.createdAt = checkNotBlank(createdAt);
    this.updatedAt = checkNotBlank(updatedAt);
    this.inputDatasetUrns = (List<String>) checkNotEmpty(inputDatasetUrns);
    this.outputDatasetUrns = (List<String>) checkNotEmpty(outputDatasetUrns);
    this.location = checkNotBlank(location);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(this.description);
  }
}
