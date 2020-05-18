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

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public class NamespaceMeta {
  @Getter private final String ownerName;
  @Nullable private final String description;

  public NamespaceMeta(@NonNull final String ownerName, @Nullable final String description) {
    this.ownerName = ownerName;
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public String toJson() {
    return Utils.toJson(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String ownerName;
    @Nullable private String description;

    public Builder ownerName(@NonNull String ownerName) {
      this.ownerName = ownerName;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public NamespaceMeta build() {
      return new NamespaceMeta(ownerName, description);
    }
  }
}
