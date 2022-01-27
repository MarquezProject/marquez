/* SPDX-License-Identifier: Apache-2.0 */

package marquez.db.models;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DatasetVersionRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final UUID datasetUuid;
  @Getter @NonNull private final UUID version;
  @Nullable private final UUID runUuid;

  public Optional<UUID> getRunUuid() {
    return Optional.ofNullable(runUuid);
  }
}
