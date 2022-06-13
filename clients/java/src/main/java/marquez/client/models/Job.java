/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.time.Instant;
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

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Job extends JobMeta {
  @Getter private final JobId id;
  @Getter private final String name;
  @Getter private final String simpleName;
  @Getter private final String parentJobName;
  @Getter private final Instant createdAt;
  @Getter private final Instant updatedAt;
  @Getter private final String namespace;
  @Nullable private final Run latestRun;
  @Getter private final Map<String, Object> facets;
  @Nullable private final UUID currentVersion;

  public Job(
      @NonNull final JobId id,
      final JobType type,
      @NonNull final String name,
      @NonNull final String simpleName,
      final String parentJobName,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @NonNull final String namespace,
      final Set<DatasetId> inputs,
      final Set<DatasetId> outputs,
      @Nullable final URL location,
      final Map<String, String> context,
      final String description,
      @Nullable final Run latestRun,
      @Nullable final Map<String, Object> facets,
      @Nullable UUID currentVersion) {
    super(type, inputs, outputs, location, context, description, null);
    this.id = id;
    this.name = name;
    this.simpleName = simpleName;
    this.parentJobName = parentJobName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.namespace = namespace;
    this.latestRun = latestRun;
    this.facets = (facets == null) ? ImmutableMap.of() : ImmutableMap.copyOf(facets);
    this.currentVersion = currentVersion;
  }

  public Optional<Run> getLatestRun() {
    return Optional.ofNullable(latestRun);
  }

  public boolean hasFacets() {
    return !facets.isEmpty();
  }

  public static Job fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<Job>() {});
  }

  public Optional<UUID> getCurrentVersion() {
    return Optional.ofNullable(currentVersion);
  }
}
