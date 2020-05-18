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
import java.net.URL;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonPropertyOrder({"type", "physicalName", "sourceName", "schemaLocation", "description", "runId"})
public final class StreamMeta extends DatasetMeta {
  @Getter private final URL schemaLocation;

  @Builder
  public StreamMeta(
      final String physicalName,
      final String sourceName,
      @NonNull final URL schemaLocation,
      @Nullable final List<Field> fields,
      @Nullable final List<String> tags,
      @Nullable final String description,
      @Nullable final String runId) {
    super(physicalName, sourceName, fields, tags, description, runId);
    this.schemaLocation = schemaLocation;
  }

  @Override
  public String toJson() {
    return Utils.toJson(this);
  }

  public static class StreamMetaBuilder {
    private URL schemaLocation;

    public StreamMetaBuilder schemaLocation(@NonNull String schemaLocationString) {
      return schemaLocation(Utils.toUrl(schemaLocationString));
    }

    public StreamMetaBuilder schemaLocation(@NonNull URL schemaLocation) {
      this.schemaLocation = schemaLocation;
      return this;
    }
  }
}
