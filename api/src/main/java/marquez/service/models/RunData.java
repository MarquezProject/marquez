package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import marquez.common.models.DatasetId;
import marquez.common.models.InputDatasetVersion;
import marquez.common.models.JobVersionId;
import marquez.common.models.OutputDatasetVersion;
import marquez.common.models.RunState;

@Value
@With
public class RunData implements NodeData {
  @NonNull UUID uuid;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @Nullable Instant startedAt;
  @Nullable Instant endedAt;
  @NonNull RunState state;
  @NonNull UUID jobUuid;
  @Nullable JobVersionId jobVersionId;
  @NonNull List<UUID> inputUuids;
  @NonNull List<UUID> outputUuids;
  int depth;
  @With @Nullable ImmutableSet<DatasetId> inputs;
  @With @Nullable ImmutableSet<DatasetId> outputs;
  @Nullable List<InputDatasetVersion> inputDatasetVersions;
  @Nullable List<OutputDatasetVersion> outputDatasetVersions;
  @Nullable List<UUID> childRunIds;
  @Nullable List<UUID> parentRunIds;
  @Nullable ImmutableMap<String, Object> facets;

  public Optional<Instant> getStartedAt() {
    return Optional.ofNullable(startedAt);
  }

  public Optional<Instant> getEndedAt() {
    return Optional.ofNullable(endedAt);
  }

  public UUID getUuid() {
    return uuid;
  }

  @JsonIgnore
  public Set<UUID> getInputUuids() {
    return ImmutableSet.copyOf(inputUuids);
  }

  @JsonIgnore
  public Set<UUID> getOutputUuids() {
    return ImmutableSet.copyOf(outputUuids);
  }
}
