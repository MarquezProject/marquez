/*
 * Copyright 2018-2023 contributors to the Marquez project
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
import lombok.Value;

@AllArgsConstructor
@EqualsAndHashCode
@Value
public class DatasetSymlinkRow {
  @NonNull UUID uuid;
  @NonNull String name;
  @NonNull UUID namespaceUuid;
  @Nullable String type;
  @NonNull boolean isPrimary;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }
}
