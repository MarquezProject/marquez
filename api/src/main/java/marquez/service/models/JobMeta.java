/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.JobType;
import marquez.common.models.RunId;

@EqualsAndHashCode
@ToString
public final class JobMeta {
  @Getter private final JobType type;
  @Getter private final ImmutableSet<DatasetId> inputs;
  @Getter private final ImmutableSet<DatasetId> outputs;
  @Nullable private final URL location;
  @Getter private final ImmutableMap<String, String> context;
  @Nullable private final String description;
  @Nullable private final RunId runId;

  public JobMeta(
      @NonNull final JobType type,
      @NonNull final ImmutableSet<DatasetId> inputs,
      @NonNull final ImmutableSet<DatasetId> outputs,
      @Nullable final URL location,
      @Nullable final ImmutableMap<String, String> context,
      @Nullable final String description,
      @Nullable final RunId runId) {
    this.type = type;
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : context;
    this.description = description;
    this.runId = runId;
  }

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<RunId> getRunId() {
    return Optional.ofNullable(runId);
  }
}
