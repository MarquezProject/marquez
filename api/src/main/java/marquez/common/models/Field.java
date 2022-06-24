/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Field {
  @JsonUnwrapped
  @JsonProperty(access = READ_ONLY)
  @Getter
  private final FieldName name;

  @Nullable @Getter private final String type;
  @Getter private final ImmutableSet<TagName> tags;
  @Nullable private final String description;

  @JsonCreator
  public Field(
      @JsonProperty("name") final String nameAsString,
      @JsonProperty("type") final String typeAsString,
      @JsonProperty("tags") final ImmutableSet<String> tagsAsString,
      final String description) {
    this(
        FieldName.of(nameAsString),
        typeAsString,
        (tagsAsString == null)
            ? ImmutableSet.of()
            : tagsAsString.stream().map(TagName::of).collect(toImmutableSet()),
        description);
  }

  public Field(
      @NonNull final FieldName name,
      @Nullable final String type,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String description) {
    this.name = name;
    this.type = type;
    this.tags = (tags == null) ? ImmutableSet.of() : tags;
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
