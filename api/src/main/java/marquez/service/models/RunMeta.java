/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.common.models.RunId;

@EqualsAndHashCode
@ToString
@Builder
public final class RunMeta {
  @Nullable private final RunId id;
  @Nullable private final Instant nominalStartTime;
  @Nullable private final Instant nominalEndTime;
  @Getter private final Map<String, String> args;

  public RunMeta(
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @Nullable final Map<String, String> args) {
    this(null, nominalStartTime, nominalEndTime, args);
  }

  @JsonCreator
  public RunMeta(
      @Nullable final RunId id,
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @Nullable final Map<String, String> args) {
    this.id = id;
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.args = (args == null) ? ImmutableMap.of() : args;
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public Optional<RunId> getId() {
    return Optional.ofNullable(id);
  }
}
