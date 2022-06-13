/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Namespace extends NamespaceMeta {
  @Getter private final String name;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;

  public Namespace(
      @NonNull final String name,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      final String ownerName,
      @Nullable final String description) {
    super(ownerName, description);
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Namespace fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<Namespace>() {});
  }
}
