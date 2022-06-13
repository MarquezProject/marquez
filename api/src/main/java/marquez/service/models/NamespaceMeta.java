/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.OwnerName;

@Value
public class NamespaceMeta {
  @NonNull OwnerName ownerName;
  @Nullable String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
