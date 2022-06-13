/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public final class JobVersion {
  @Getter private final JobVersionId id;
  @Getter private final String name;
  @Getter private final Instant createdAt;
  @Getter private final UUID version;
  @Getter private final String namespace;
  @Nullable private final URL location;
  @Getter private final ImmutableMap<String, String> context;
  @Getter private final List<DatasetId> inputs;
  @Getter private final List<DatasetId> outputs;
  @Getter @Nullable private final Run latestRun;

  public JobVersion(
      @NonNull final JobVersionId id,
      @NonNull final String name,
      @NonNull final Instant createdAt,
      @NonNull final UUID version,
      @Nullable final URL location,
      @Nullable final ImmutableMap<String, String> context,
      List<DatasetId> inputs,
      List<DatasetId> outputs,
      @Nullable Run latestRun) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.version = version;
    this.namespace = id.getNamespace();
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : context;
    this.inputs = inputs;
    this.outputs = outputs;
    this.latestRun = latestRun;
  }

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public static JobVersion fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<JobVersion>() {});
  }
}
