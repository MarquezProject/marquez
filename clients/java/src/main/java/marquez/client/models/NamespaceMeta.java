/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public class NamespaceMeta {
  @Getter private final String ownerName;
  @Nullable private final String description;

  public NamespaceMeta(@NonNull final String ownerName, @Nullable final String description) {
    this.ownerName = ownerName;
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public String toJson() {
    return Utils.toJson(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String ownerName;
    @Nullable private String description;

    public Builder ownerName(@NonNull String ownerName) {
      this.ownerName = ownerName;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public NamespaceMeta build() {
      return new NamespaceMeta(ownerName, description);
    }
  }
}
