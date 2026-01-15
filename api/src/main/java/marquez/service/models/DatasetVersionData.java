/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;
import marquez.common.models.Version;

@EqualsAndHashCode
@ToString
public final class DatasetVersionData implements NodeData {
  @NonNull private final DatasetVersion version;
  @Nullable @Setter private UUID uuid;
  @Nullable @Setter private UUID createdByParentRunUuid;

  public DatasetVersionData(DatasetVersion version) {
    this.version = version;
  }

  @JsonIgnore
  public DatasetVersion getDatasetVersion() {
    return version;
  }

  public UUID getUuid() {
    return uuid;
  }

  public NamespaceName getNamespace() {
    return version.getNamespace();
  }

  public DatasetName getName() {
    return version.getName();
  }

  public DatasetName getPhysicalName() {
    return version.getPhysicalName();
  }

  public SourceName getSourceName() {
    return version.getSourceName();
  }

  public ImmutableList<Field> getFields() {
    return version.getFields();
  }

  public ImmutableSet<TagName> getTags() {
    return version.getTags();
  }

  public Optional<String> getDescription() {
    return version.getDescription();
  }

  public Optional<UUID> getCurrentSchemaVersion() {
    return version.getCurrentSchemaVersion();
  }

  public String getLifecycleState() {
    return version.getLifecycleState().orElse(null);
  }

  public UUID getCreatedByRunUuid() {
    return version.getCreatedByRunUuid();
  }

  public UUID getCreatedByParentRunUuid() {
    return createdByParentRunUuid;
  }

  public Optional<Run> getCreatedByRun() {
    return version.getCreatedByRun();
  }

  public ImmutableMap<String, Object> getFacets() {
    return version.getFacets();
  }

  public Version getVersion() {
    return version.getVersion();
  }

  public Instant getCreatedAt() {
    return version.getCreatedAt();
  }
}
