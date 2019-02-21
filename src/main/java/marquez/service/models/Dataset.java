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
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class Dataset {
  @Getter @NonNull private final DatasetUrn urn;
  @Getter @NonNull private final Instant createdAt;
  private final Description description;

  public Optional<Description> getDescription() {
    return Optional.ofNullable(description);
  }
}
