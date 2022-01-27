/* SPDX-License-Identifier: Apache-2.0 */

package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import lombok.Value;

@Value
public class RunStateRow {
  @NonNull UUID uuid;
  @NonNull Instant transitionedAt;
  @NonNull UUID runUuid;
  @NonNull String state;
}
