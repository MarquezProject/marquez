/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

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
import marquez.common.models.TagName;

@EqualsAndHashCode
@ToString
public final class JobMeta {
  @Getter private final JobType type;
  @Getter private final ImmutableSet<DatasetId> inputs;
  @Getter private final ImmutableSet<DatasetId> outputs;
  @Nullable private final URL location;
  @Nullable private final String description;
  @Nullable private final RunId runId;
  @Getter private final ImmutableSet<TagName> tags;

  public JobMeta(
      @NonNull final JobType type,
      @NonNull final ImmutableSet<DatasetId> inputs,
      @NonNull final ImmutableSet<DatasetId> outputs,
      @Nullable final URL location,
      @Nullable final String description,
      @Nullable final RunId runId,
      @Nullable final ImmutableSet<TagName> tags) {
    this.type = type;
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.description = description;
    this.runId = runId;
    this.tags = tags;
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
