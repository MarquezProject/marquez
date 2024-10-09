/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.time.Instant;
import java.util.List;
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
import marquez.common.models.TagName;

@EqualsAndHashCode
@ToString
public final class Job {
  @Getter private final JobId id;
  @Getter private final JobType type;
  @Getter private final JobName name;
  @Getter private final String simpleName;
  @Getter private final String parentJobName;
  @Getter private final UUID parentJobUuid;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter private final NamespaceName namespace;
  @Getter @Setter private Set<DatasetId> inputs;
  @Getter @Setter private Set<DatasetId> outputs;
  @Nullable private final URL location;
  @Nullable private final String description;
  @Nullable @Setter private Run latestRun;
  @Nullable @Setter private List<Run> latestRuns;
  @Getter private final ImmutableMap<String, Object> facets;
  @Nullable private UUID currentVersion;
  @Getter @Nullable private ImmutableList<String> labels;
  @Getter @Nullable private final ImmutableSet<TagName> tags;

  public Job(
      @NonNull final JobId id,
      @NonNull final JobType type,
      @NonNull final JobName name,
      @NonNull String simpleName,
      @Nullable String parentJobName,
      @Nullable UUID parentJobUuid,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @NonNull final Set<DatasetId> inputs,
      @NonNull final Set<DatasetId> outputs,
      @Nullable final URL location,
      @Nullable final String description,
      @Nullable final Run latestRun,
      @Nullable final List<Run> latestRuns,
      @Nullable final ImmutableMap<String, Object> facets,
      @Nullable UUID currentVersion,
      @Nullable ImmutableList<String> labels,
      @Nullable final ImmutableSet<TagName> tags) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.simpleName = simpleName;
    this.parentJobName = parentJobName;
    this.parentJobUuid = parentJobUuid;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.namespace = id.getNamespace();
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.description = description;
    this.latestRun = latestRun;
    this.latestRuns = latestRuns;
    this.facets = (facets == null) ? ImmutableMap.of() : facets;
    this.currentVersion = currentVersion;
    this.labels = (labels == null) ? ImmutableList.of() : labels;
    this.tags = (tags == null) ? ImmutableSet.of() : tags;
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

  public Optional<List<Run>> getLatestRuns() {
    return Optional.ofNullable(latestRuns);
  }

  public Optional<UUID> getCurrentVersion() {
    return Optional.ofNullable(currentVersion);
  }
}
