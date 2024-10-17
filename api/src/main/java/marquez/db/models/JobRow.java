/*
 * Copyright 2018-2023 contributors to the Marquez project
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
  @Nullable UUID namespaceUuid;
  @NonNull String namespaceName;
  @NonNull String name;
  @NonNull String simpleName;
  @Nullable String parentJobName;
  @Nullable UUID parentJobUuid;
  @Nullable String description;
  @Nullable UUID currentVersionUuid;
  @Nullable String location;
  @Nullable Set<DatasetId> inputs;
  @Nullable UUID symlinkTargetId;
  @Nullable UUID currentRunUuid;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<UUID> getCurrentVersionUuid() {
    return Optional.ofNullable(currentVersionUuid);
  }

  public Optional<UUID> getCurrentRunUuid() {
    return Optional.ofNullable(currentRunUuid);
  }
}
