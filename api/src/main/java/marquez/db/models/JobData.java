/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.service.models.Run;

@Getter
@AllArgsConstructor
@ToString(of = {"namespace", "name", "type"})
public class JobData implements NodeData {
  UUID uuid;
  @NonNull JobId id;
  @NonNull JobType type;
  @NonNull JobName name;
  @NonNull String simpleName;
  @Nullable String parentJobName;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull NamespaceName namespace;
  @Setter ImmutableSet<DatasetId> inputs = ImmutableSet.of();
  @Setter ImmutableSet<UUID> inputUuids = ImmutableSet.of();
  @Setter ImmutableSet<DatasetId> outputs = ImmutableSet.of();
  @Setter ImmutableSet<UUID> outputUuids = ImmutableSet.of();
  @Nullable URL location;
  @NonNull ImmutableMap<String, String> context;
  @Nullable String description;
  @Nullable @Setter Run latestRun;

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<Run> getLatestRun() {
    return Optional.ofNullable(latestRun);
  }

  @JsonIgnore
  public UUID getUuid() {
    return uuid;
  }

  @JsonIgnore
  public Set<UUID> getInputUuids() {
    return inputUuids;
  }

  @JsonIgnore
  public Set<UUID> getOutputUuids() {
    return outputUuids;
  }
}
