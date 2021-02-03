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

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.Utils;
import marquez.common.models.SourceType;

@EqualsAndHashCode
@ToString
public final class SourceMeta {
  @Getter private final SourceType type;
  @Nullable private final URI connectionUrl;
  @Nullable private final String description;

  public SourceMeta(@NonNull final SourceType type, @Nullable final String description) {
    this(type, null, description);
  }

  public SourceMeta(@NonNull final URI connectionUrl, @Nullable final String description) {
    this(null, connectionUrl, description);
  }

  @JsonCreator
  public SourceMeta(
      @Nullable final SourceType type,
      @Nullable final URI connectionUrl,
      @Nullable final String description) {
    // ...
    this.type =
        (connectionUrl == null)
            ? checkNotNull(type, "connection url must be provided when type == null")
            : Utils.sourceTypeFor(connectionUrl);
    // ...
    this.connectionUrl = (connectionUrl == null) ? null : Utils.urlWithNoCredentials(connectionUrl);
    this.description = description;
  }

  public Optional<URI> getConnectionUrl() {
    return Optional.ofNullable(connectionUrl);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
