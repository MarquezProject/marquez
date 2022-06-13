/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public class RunMeta {
  @Getter @Nullable private final String id;
  @Nullable private final Instant nominalStartTime;
  @Nullable private final Instant nominalEndTime;
  @Getter private final Map<String, String> args;

  public RunMeta(
      @Nullable final String id,
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @Nullable final Map<String, String> args) {
    this.id = id;
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.args = (args == null) ? ImmutableMap.of() : ImmutableMap.copyOf(args);
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public String toJson() {
    return Utils.toJson(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    @Nullable private String id;
    @Nullable private Instant nominalStartTime;
    @Nullable private Instant nominalEndTime;
    @Nullable private Map<String, String> args;

    private Builder() {
      this.args = ImmutableMap.of();
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder nominalStartTime(@Nullable Instant nominalStartTime) {
      this.nominalStartTime = nominalStartTime;
      return this;
    }

    public Builder nominalEndTime(@Nullable Instant nominalEndTime) {
      this.nominalEndTime = nominalEndTime;
      return this;
    }

    public Builder args(@Nullable Map<String, String> args) {
      this.args = (args == null) ? ImmutableMap.of() : ImmutableMap.copyOf(args);
      return this;
    }

    public RunMeta build() {
      return new RunMeta(id, nominalStartTime, nominalEndTime, args);
    }
  }
}
