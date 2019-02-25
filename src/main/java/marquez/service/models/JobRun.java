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

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class JobRun {
  private final UUID guid;
  private final Integer currentState;
  private final UUID jobVersionGuid;
  private final String runArgsHexDigest;
  private final String runArgs;
  private final Timestamp nominalStartTime;
  private final Timestamp nominalEndTime;
  private final Timestamp createdAt;
}
