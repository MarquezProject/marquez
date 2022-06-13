/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Field {
  @Getter private final String name;
  @Nullable private final String type;
  @Getter private final Set<String> tags;
  @Nullable String description;

  public Field(
      @NonNull final String name,
      @Nullable final String type,
      @Nullable final Set<String> tags,
      @Nullable final String description) {
    this.name = name;
    this.type = type;
    this.tags = (tags == null) ? ImmutableSet.of() : ImmutableSet.copyOf(tags);
    this.description = description;
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String name;
    @Nullable private String type;
    private Set<String> tags;
    @Nullable private String description;

    private Builder() {
      this.tags = Sets.newHashSet();
    }

    public Builder name(@NonNull String name) {
      this.name = name;
      return this;
    }

    public Builder type(@Nullable String type) {
      this.type = type;
      return this;
    }

    public Builder tags(@NonNull Set<String> tags) {
      this.tags = tags;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public Field build() {
      return new Field(name, type, tags, description);
    }
  }
}
