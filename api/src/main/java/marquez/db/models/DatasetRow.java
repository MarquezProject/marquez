/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

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
import lombok.With;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DatasetRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final String type;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  @Getter @NonNull private final UUID namespaceUuid;
  @Getter @NonNull private final UUID sourceUuid;
  @Getter @NonNull private final String name;
  @Getter @NonNull private final String physicalName;
  @Nullable private final Instant lastModifiedAt;
  @Nullable private final String description;
  @With @Nullable private final UUID currentVersionUuid;
  @Getter private final boolean isDeleted;

  public Optional<Instant> getLastModifiedAt() {
    return Optional.ofNullable(lastModifiedAt);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<UUID> getCurrentVersionUuid() {
    return Optional.ofNullable(currentVersionUuid);
  }
}
