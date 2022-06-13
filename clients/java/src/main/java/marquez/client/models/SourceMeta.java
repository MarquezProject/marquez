/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public class SourceMeta {
  @Getter private final String type;
  @Getter private final URI connectionUrl;
  @Nullable private final String description;

  public SourceMeta(
      @NonNull final String type,
      @NonNull final URI connectionUrl,
      @Nullable final String description) {
    this.type = type;
    this.connectionUrl = connectionUrl;
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
    private String type;
    private URI connectionUrl;
    @Nullable private String description;

    public Builder type(@NonNull String type) {
      this.type = type;
      return this;
    }

    public Builder connectionUrl(@NonNull String connectionUrlAsString) {
      return connectionUrl(URI.create(connectionUrlAsString));
    }

    public Builder connectionUrl(@NonNull URI connectionUrl) {
      this.connectionUrl = connectionUrl;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public SourceMeta build() {
      return new SourceMeta(type, connectionUrl, description);
    }
  }
}
