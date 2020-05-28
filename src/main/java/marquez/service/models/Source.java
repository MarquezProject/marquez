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

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;

@Value
public class Source {
  @NonNull SourceType type;

  @NonNull
  @JsonUnwrapped
  @JsonProperty(access = READ_ONLY)
  SourceName name;

  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull URI connectionUrl;
  @Nullable String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
