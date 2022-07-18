/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;

@Value
@EqualsAndHashCode(of = "id")
public class DatasetData implements NodeData {
  UUID uuid;
  @NonNull DatasetId id;
  @NonNull DatasetType type;
  @NonNull DatasetName name;
  @NonNull DatasetName physicalName;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull NamespaceName namespace;
  @NonNull SourceName sourceName;
  @NonNull ImmutableList<Field> fields;
  @NonNull ImmutableSet<TagName> tags;
  @Nullable Instant lastModifiedAt;
  @Nullable String description;
  @Nullable String lastLifecycleState;

  public Optional<Instant> getLastModifiedAt() {
    return Optional.ofNullable(lastModifiedAt);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<String> getLastLifecycleState() {
    return Optional.ofNullable(lastLifecycleState);
  }

  @JsonIgnore
  public UUID getUuid() {
    return uuid;
  }
}
