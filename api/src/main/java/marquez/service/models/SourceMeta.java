/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import jakarta.annotation.Nullable;
import java.net.URI;
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.SourceType;

@Value
public class SourceMeta {
  @NonNull SourceType type;
  @NonNull URI connectionUrl;
  @Nullable String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
