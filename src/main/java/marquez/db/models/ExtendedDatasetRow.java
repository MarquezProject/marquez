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
public final class ExtendedDatasetRow extends DatasetRow {
  @Getter private String sourceName;

  public ExtendedDatasetRow(
      final UUID uuid,
      final String type,
      final Instant createdAt,
      final Instant updatedAt,
      final UUID namespaceUuid,
      final UUID sourceUuid,
      @NonNull final String sourceName,
      final String name,
      final String physicalName,
      @Nullable final String description,
      @Nullable final UUID currentVersionUuid) {
    super(
        uuid,
        type,
        createdAt,
        updatedAt,
        namespaceUuid,
        sourceUuid,
        name,
        physicalName,
        description,
        currentVersionUuid);
    this.sourceName = sourceName;
  }
}
