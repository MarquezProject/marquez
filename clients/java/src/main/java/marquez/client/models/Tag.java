/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public final class Tag {
  @Getter private final String name;
  @Nullable private final String description;

  public Tag(@NonNull final String name, @Nullable final String description) {
    this.name = name;
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public static Tag fromJson(String tagAsJson) {
    return Utils.fromJson(tagAsJson, new TypeReference<Tag>() {});
  }

  public String toJson() {
    return Utils.toJson(this);
  }
}
