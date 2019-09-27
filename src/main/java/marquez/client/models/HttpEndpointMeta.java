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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonPropertyOrder({"type", "physicalName", "sourceName", "httpMethod", "description", "runId"})
public final class HttpEndpointMeta extends DatasetMeta {
  @Getter private final HttpMethod httpMethod;

  public HttpEndpointMeta(
      final String physicalName,
      final String sourceName,
      @NonNull final HttpMethod httpMethod,
      @Nullable final String description,
      @Nullable final String runId) {
    super(physicalName, sourceName, description, runId);
    this.httpMethod = httpMethod;
  }

  @Override
  public String toJson() {
    return Utils.toJson(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String physicalName;
    private String sourceName;
    private HttpMethod httpMethod;
    @Nullable private String description;
    @Nullable private String runId;

    public Builder physicalName(@NonNull String physicalName) {
      this.physicalName = physicalName;
      return this;
    }

    public Builder sourceName(@NonNull String sourceName) {
      this.sourceName = sourceName;
      return this;
    }

    public Builder httpMethod(@NonNull String httpMethodString) {
      return httpMethod(HttpMethod.valueOf(httpMethodString));
    }

    public Builder httpMethod(@NonNull HttpMethod httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public Builder runId(@Nullable String runId) {
      this.runId = runId;
      return this;
    }

    public HttpEndpointMeta build() {
      return new HttpEndpointMeta(physicalName, sourceName, httpMethod, description, runId);
    }
  }
}
