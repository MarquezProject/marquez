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
import lombok.Data;

@Data
@Builder
public class DatasetRowExtended {
  private UUID dsUuid;
  private String dsName;
  private String dsDescription;
  private Instant dsCreatedAt;
  private Instant dsUpdatedAt;
  private UUID dfUuid;
  private String dfName;
  private String dfType;
  private String dfDescription;
  private Instant dfCreatedAt;
  private Instant dfUpdatedAt;
  private String tagName;
  private Instant taggedAt;
}
