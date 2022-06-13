/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

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
  @Getter @NonNull private final DatasetId id;
  @Getter @NonNull private final DatasetType type;
  @Getter @NonNull private final String name;
  @Getter @NonNull private final String physicalName;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  @Getter @NonNull private final String namespace;
  @Getter @NonNull private final String sourceName;
  @Getter @NonNull private final List<Field> fields;
  @Getter @NonNull private final Set<String> tags;
  @Nullable private final Instant lastModifiedAt;
  @Nullable private final String description;
  @Getter private final Map<String, Object> facets;
  @Nullable private final UUID currentVersion;

  public Dataset(
      @NonNull final DatasetId id,
      @NonNull final DatasetType type,
      @NonNull final String name,
      @NonNull final String physicalName,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @NonNull final String namespace,
      @NonNull final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final Set<String> tags,
      @Nullable final Instant lastModifiedAt,
      @Nullable final String description,
      @Nullable final Map<String, Object> facets,
      @Nullable final UUID currentVersion) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.physicalName = physicalName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.namespace = namespace;
    this.sourceName = sourceName;
    this.fields = (fields == null) ? ImmutableList.of() : ImmutableList.copyOf(fields);
    this.tags = (tags == null) ? ImmutableSet.of() : ImmutableSet.copyOf(tags);
    this.lastModifiedAt = lastModifiedAt;
    this.description = description;
    this.facets = (facets == null) ? ImmutableMap.of() : ImmutableMap.copyOf(facets);
    this.currentVersion = currentVersion;
  }

  public Optional<Instant> getLastModifiedAt() {
    return Optional.ofNullable(lastModifiedAt);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public boolean hasFacets() {
    return !facets.isEmpty();
  }

  public static Dataset fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<Dataset>() {});
  }

  public Optional<UUID> getCurrentVersion() {
    return Optional.ofNullable(currentVersion);
  }
}
