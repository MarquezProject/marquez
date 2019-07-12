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

import static marquez.common.base.MorePreconditions.checkNotBlank;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.*;

@EqualsAndHashCode
@ToString
public class NamespaceResponse {
  @Getter String name;
  @Getter String createdAt;
  @Getter String owner;
  String description;

  public NamespaceResponse(
      @NonNull String name,
      @NonNull String createdAt,
      @NonNull String owner,
      @Nullable String description) {
    this.name = checkNotBlank(name);
    this.createdAt = checkNotBlank(createdAt);
    this.owner = checkNotBlank(owner);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
