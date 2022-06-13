/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.TagName;

@EqualsAndHashCode
@ToString
public final class Tag {
  @Getter
  @JsonUnwrapped
  @JsonProperty(access = READ_ONLY)
  private final TagName name;

  @Nullable String description;

  @JsonCreator
  public Tag(
      @JsonProperty("name") @NonNull final String nameAsString,
      @JsonProperty("description") @Nullable final String description) {
    this(TagName.of(nameAsString), description);
  }

  public Tag(@NonNull final TagName name, @Nullable final String description) {
    this.name = name;
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
