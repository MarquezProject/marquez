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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public final class DatasetRowExtended extends DatasetRow {
  @Getter private String datasourceUrn;

  @Builder(builderMethodName = "builderExtended")
  public DatasetRowExtended(
      final UUID uuid,
      final Instant createdAt,
      final Instant updatedAt,
      final UUID namespaceUuid,
      final UUID datasourceUuid,
      final String name,
      final String urn,
      final String datasourceUrn,
      final String description,
      final UUID currentVersionUuid) {
    super(
        uuid,
        createdAt,
        updatedAt,
        namespaceUuid,
        datasourceUuid,
        name,
        urn,
        description,
        currentVersionUuid);
    this.datasourceUrn = datasourceUrn;
  }
}
