/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
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
import marquez.common.models.Version;

@EqualsAndHashCode
@ToString
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTableVersion.class, name = "DB_TABLE"),
  @JsonSubTypes.Type(value = StreamVersion.class, name = "STREAM")
})
public abstract class DatasetVersion {
  @Getter private final DatasetId id;
  @Getter private final DatasetType type;
  @Getter private final DatasetName name;
  @Getter private final DatasetName physicalName;
  @Getter private final Instant createdAt;
  @Getter private final Version version;
  @Getter private final NamespaceName namespace;
  @Getter private final SourceName sourceName;
  @Getter @Setter private ImmutableList<Field> fields;
  @Getter @Setter private ImmutableSet<TagName> tags;
  @Nullable private final String lifecycleState;
  @Nullable private final String description;
  @Nullable @Setter private Run createdByRun;
  @Nullable @Setter private UUID createdByRunUuid;
  @Getter private final ImmutableMap<String, Object> facets;

  public DatasetVersion(
      @NonNull final DatasetId id,
      @NonNull final DatasetType type,
      @NonNull final DatasetName name,
      @NonNull final DatasetName physicalName,
      @NonNull final Instant createdAt,
      @NonNull final Version version,
      @NonNull final SourceName sourceName,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String lifecycleState,
      @Nullable final String description,
      @Nullable final Run createdByRun,
      @Nullable final ImmutableMap<String, Object> facets) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.physicalName = physicalName;
    this.createdAt = createdAt;
    this.version = version;
    this.namespace = id.getNamespace();
    this.sourceName = sourceName;
    this.fields = (fields == null) ? ImmutableList.of() : fields;
    this.tags = (tags == null) ? ImmutableSet.of() : tags;
    this.lifecycleState = lifecycleState;
    this.description = description;
    this.createdByRun = createdByRun;
    this.facets = (facets == null) ? ImmutableMap.of() : facets;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<Run> getCreatedByRun() {
    return Optional.ofNullable(createdByRun);
  }

  public Optional<String> getLifecycleState() {
    return Optional.ofNullable(lifecycleState);
  }

  @JsonIgnore
  public UUID getCreatedByRunUuid() {
    return createdByRunUuid;
  }
}
