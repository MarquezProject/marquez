/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;

@EqualsAndHashCode
@ToString
public final class Job {
  @Getter private final JobId id;
  @Getter private final JobType type;
  @Getter private final JobName name;
  @Getter private final String simpleName;
  @Getter private final String parentJobName;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter private final NamespaceName namespace;
  @Getter @Setter private Set<DatasetId> inputs;
  @Getter @Setter private Set<DatasetId> outputs;
  @Nullable private final URL location;
  @Getter private final ImmutableMap<String, String> context;
  @Nullable private final String description;
  @Nullable @Setter private Run latestRun;
  @Getter private final ImmutableMap<String, Object> facets;
  @Nullable private UUID currentVersion;

  public Job(
      @NonNull final JobId id,
      @NonNull final JobType type,
      @NonNull final JobName name,
      @NonNull String simpleName,
      @Nullable String parentJobName,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @NonNull final Set<DatasetId> inputs,
      @NonNull final Set<DatasetId> outputs,
      @Nullable final URL location,
      @Nullable final ImmutableMap<String, String> context,
      @Nullable final String description,
      @Nullable final Run latestRun,
      @Nullable final ImmutableMap<String, Object> facets,
      @Nullable UUID currentVersion) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.simpleName = simpleName;
    this.parentJobName = parentJobName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.namespace = id.getNamespace();
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : context;
    this.description = description;
    this.latestRun = latestRun;
    this.facets = (facets == null) ? ImmutableMap.of() : facets;
    this.currentVersion = currentVersion;
  }

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<Run> getLatestRun() {
    return Optional.ofNullable(latestRun);
  }

  public Optional<UUID> getCurrentVersion() {
    return Optional.ofNullable(currentVersion);
  }
}
