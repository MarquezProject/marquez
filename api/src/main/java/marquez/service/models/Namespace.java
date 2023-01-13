/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;

@Value
public class Namespace {
  @NonNull NamespaceName name;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull OwnerName ownerName;
  @Nullable String description;
  @NonNull Boolean isHidden;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
