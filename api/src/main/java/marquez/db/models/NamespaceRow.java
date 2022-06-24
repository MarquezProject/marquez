/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class NamespaceRow {
  @NonNull UUID uuid;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull String name;
  @Nullable String description;
  @NonNull String currentOwnerName;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
