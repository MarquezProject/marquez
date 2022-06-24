/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class JobVersionRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updateAt;
  @Getter @NonNull private final UUID jobUuid;
  @Getter @NonNull private final String jobName;
  @Getter @NonNull private final UUID jobContextUuid;
  @Getter @NonNull private final List<UUID> inputUuids;
  @Getter @NonNull private final List<UUID> outputUuids;
  @Nullable private final String location;
  @Getter @NonNull private final UUID version;
  @Nullable private final UUID latestRunUuid;
  @Getter @NonNull private final UUID namespaceUuid;
  @Getter @NonNull private final String namespaceName;

  public boolean hasInputUuids() {
    return !inputUuids.isEmpty();
  }

  public boolean hasOutputUuids() {
    return !outputUuids.isEmpty();
  }

  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<UUID> getLatestRunUuid() {
    return Optional.ofNullable(latestRunUuid);
  }
}
