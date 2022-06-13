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
  @JsonSubTypes.Type(value = DbTableVersion.class, name = "DB_TABLE"),
  @JsonSubTypes.Type(value = StreamVersion.class, name = "STREAM")
})
public abstract class DatasetVersion {
  @Getter @NonNull private final DatasetId id;
  @Getter @NonNull private final DatasetType type;
  @Getter @NonNull private final String name;
  @Getter @NonNull private final String physicalName;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final String version;
  @Getter @NonNull private final String namespace;
  @Getter @NonNull private final String sourceName;
  @Getter @NonNull private final List<Field> fields;
  @Getter @NonNull private final Set<String> tags;
  @Nullable private final String description;
  @Nullable private final Run createdByRun;
  @Getter private final Map<String, Object> facets;

  public DatasetVersion(
      @NonNull final DatasetId id,
      @NonNull final DatasetType type,
      @NonNull final String name,
      @NonNull final String physicalName,
      @NonNull final Instant createdAt,
      @NonNull final String version,
      @NonNull final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final Set<String> tags,
      @Nullable final String description,
      @Nullable final Run createdByRun,
      @Nullable final Map<String, Object> facets) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.physicalName = physicalName;
    this.createdAt = createdAt;
    this.version = version;
    this.namespace = id.getNamespace();
    this.sourceName = sourceName;
    this.fields = (fields == null) ? ImmutableList.of() : ImmutableList.copyOf(fields);
    this.tags = (tags == null) ? ImmutableSet.of() : ImmutableSet.copyOf(tags);
    this.description = description;
    this.createdByRun = createdByRun;
    this.facets = (facets == null) ? ImmutableMap.of() : ImmutableMap.copyOf(facets);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<Run> getCreatedByRun() {
    return Optional.ofNullable(createdByRun);
  }

  public boolean hasFacets() {
    return !facets.isEmpty();
  }

  public static DatasetVersion fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<DatasetVersion>() {});
  }
}
