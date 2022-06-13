/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;

@Value
public class Source {
  @NonNull SourceType type;

  @NonNull
  @JsonUnwrapped
  @JsonProperty(access = READ_ONLY)
  SourceName name;

  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull URI connectionUrl;
  @Nullable String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
