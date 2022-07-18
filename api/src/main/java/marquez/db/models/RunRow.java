/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
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
public class RunRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  @Getter private final UUID jobUuid;
  @Nullable private final UUID jobVersionUuid;
  @Nullable private final UUID parentRunUuid;
  @Getter @NonNull private final UUID runArgsUuid;
  @Nullable private final Instant nominalStartTime;
  @Nullable private final Instant nominalEndTime;
  @Nullable private final String currentRunState;
  @Nullable private final Instant startedAt;
  @Nullable private final UUID startRunStateUuid;
  @Nullable private final Instant endedAt;
  @Nullable private final UUID endRunStateUuid;

  public Optional<UUID> getParentRunUuid() {
    return Optional.ofNullable(parentRunUuid);
  }

  public Optional<UUID> getJobVersionUuid() {
    return Optional.ofNullable(jobVersionUuid);
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public Optional<String> getCurrentRunState() {
    return Optional.ofNullable(currentRunState);
  }

  public Optional<Instant> getStartedAt() {
    return Optional.ofNullable(startedAt);
  }

  public Optional<UUID> getStartRunStateUuid() {
    return Optional.ofNullable(startRunStateUuid);
  }

  public Optional<Instant> getEndedAt() {
    return Optional.ofNullable(endedAt);
  }

  public Optional<UUID> getEndRunStateUuid() {
    return Optional.ofNullable(endRunStateUuid);
  }
}
