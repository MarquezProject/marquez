/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetId;

@Value
public class JobRow {
  @NonNull UUID uuid;
  @NonNull String type;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull String namespaceName;
  @NonNull String name;
  @NonNull String simpleName;
  @Nullable String parentJobName;
  @Nullable String description;
  @Nullable UUID currentVersionUuid;
  @Nullable UUID jobContextUuid;
  @Nullable String location;
  @Nullable Set<DatasetId> inputs;
  @Nullable UUID symlinkTargetId;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<UUID> getCurrentVersionUuid() {
    return Optional.ofNullable(currentVersionUuid);
  }

  public Optional<UUID> getJobContextUuid() {
    return Optional.ofNullable(jobContextUuid);
  }
}
