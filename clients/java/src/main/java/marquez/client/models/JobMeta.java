/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode
@ToString
public class JobMeta {
  @Getter private final JobType type;
  @Getter private final Set<DatasetId> inputs;
  @Getter private final Set<DatasetId> outputs;
  @Nullable private final URL location;
  @Getter private final Map<String, String> context;
  @Nullable String description;
  @Nullable String runId;

  public JobMeta(
      @NonNull final JobType type,
      @NonNull final Set<DatasetId> inputs,
      @NonNull final Set<DatasetId> outputs,
      @Nullable final URL location,
      @Nullable final Map<String, String> context,
      @Nullable final String description,
      @Nullable String runId) {
    this.type = type;
    this.inputs = inputs;
    this.outputs = outputs;
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : ImmutableMap.copyOf(context);
    this.description = description;
    this.runId = runId;
  }

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<String> getRunId() {
    return Optional.ofNullable(runId);
  }

  public String toJson() {
    return Utils.toJson(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private JobType type;
    private Set<DatasetId> inputs;
    private Set<DatasetId> outputs;
    @Nullable private URL location;
    @Nullable private String description;
    @Nullable Map<String, String> context;
    @Nullable String runId;

    private Builder() {
      this.inputs = ImmutableSet.of();
      this.outputs = ImmutableSet.of();
    }

    public Builder type(@NonNull String typeString) {
      return type(JobType.valueOf(typeString));
    }

    public Builder type(@NonNull JobType type) {
      this.type = type;
      return this;
    }

    public Builder inputs(@NonNull String namespaceName, String... datasetNames) {
      final ImmutableSet.Builder<DatasetId> datasetIds = ImmutableSet.builder();
      for (final String datasetName : datasetNames) {
        datasetIds.add(new DatasetId(namespaceName, datasetName));
      }
      inputs(datasetIds.build());
      return this;
    }

    public Builder inputs(@NonNull Set<DatasetId> inputs) {
      this.inputs = ImmutableSet.copyOf(inputs);
      return this;
    }

    public Builder outputs(@NonNull String namespaceName, String... datasetNames) {
      final ImmutableSet.Builder<DatasetId> datasetIds = ImmutableSet.builder();
      for (final String datasetName : datasetNames) {
        datasetIds.add(new DatasetId(namespaceName, datasetName));
      }
      outputs(datasetIds.build());
      return this;
    }

    public Builder outputs(@NonNull Set<DatasetId> outputs) {
      this.outputs = ImmutableSet.copyOf(outputs);
      return this;
    }

    public Builder location(@NonNull String locationString) {
      return location(Utils.toUrl(locationString));
    }

    public Builder location(@NonNull URL location) {
      this.location = location;
      return this;
    }

    public Builder context(@Nullable Map<String, String> context) {
      this.context = context;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public Builder runId(@Nullable String runId) {
      this.runId = runId;
      return this;
    }

    public JobMeta build() {
      return new JobMeta(type, inputs, outputs, location, context, description, runId);
    }
  }
}
