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

package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StreamVersionRow extends DatasetVersionRow {
  @Getter private final String schemaLocation;

  public StreamVersionRow(
      final UUID uuid,
      final Instant createdAt,
      final UUID datasetUuid,
      final UUID version,
      @Nullable final UUID runUuid,
      @NonNull final String schemaLocation) {
    super(uuid, createdAt, datasetUuid, version, runUuid);
    this.schemaLocation = schemaLocation;
  }
}
