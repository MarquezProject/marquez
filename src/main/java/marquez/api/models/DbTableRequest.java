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

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableRequest extends DatasetRequest {
  @Getter private final List<Map<String, String>> columns;

  @JsonCreator
  public DbTableRequest(
      final String physicalName,
      final String sourceName,
      @Nullable final String description,
      @Nullable final String runId,
      @Nullable final List<Map<String, String>> columns) {
    super(physicalName, sourceName, description, runId);
    this.columns = columns;
  }
}
