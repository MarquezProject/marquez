/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;

@EqualsAndHashCode
@ToString
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTable.class, name = "DB_TABLE"),
  @JsonSubTypes.Type(value = Stream.class, name = "STREAM")
})
public abstract class Dataset {
  @Getter private final DatasetId id;
  @Getter private final DatasetType type;
  @Getter private final DatasetName name;
  @Getter private final DatasetName physicalName;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter private final NamespaceName namespace;
  @Getter private final SourceName sourceName;
  @Getter @Setter private List<Field> fields;
  @Getter private final ImmutableSet<TagName> tags;
  @Nullable private final Instant lastModifiedAt;
  @Nullable private final String lastLifecycleState;
  @Nullable private final String description;
  @Nullable private final UUID currentVersion;
  @Getter ImmutableMap<String, Object> facets;
  @Getter private final boolean isDeleted;

  public Dataset(
      @NonNull final DatasetId id,
      @NonNull final DatasetType type,
      @NonNull final DatasetName name,
      @NonNull final DatasetName physicalName,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @NonNull final SourceName sourceName,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final Instant lastModifiedAt,
      @Nullable final String lastLifecycleState,
      @Nullable final String description,
      @Nullable final UUID currentVersion,
      @Nullable final ImmutableMap<String, Object> facets,
      boolean isDeleted) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.physicalName = physicalName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.namespace = id.getNamespace();
    this.sourceName = sourceName;
    this.fields = (fields == null) ? ImmutableList.of() : fields;
    this.tags = (tags == null) ? ImmutableSet.of() : tags;
    this.lastModifiedAt = lastModifiedAt;
    this.lastLifecycleState = lastLifecycleState;
    this.description = description;
    this.currentVersion = currentVersion;
    this.facets = (facets == null) ? ImmutableMap.of() : facets;
    this.isDeleted = isDeleted;
  }

  public Optional<Instant> getLastModifiedAt() {
    return Optional.ofNullable(lastModifiedAt);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<String> getLastLifecycleState() {
    return Optional.ofNullable(lastLifecycleState);
  }

  public Optional<UUID> getCurrentVersion() {
    return Optional.ofNullable(currentVersion);
  }
}
