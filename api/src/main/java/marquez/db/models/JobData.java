package marquez.db.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.service.models.Run;

@Getter
@AllArgsConstructor
public class JobData implements NodeData {
  UUID uuid;
  @NonNull JobId id;
  @NonNull JobType type;
  @NonNull JobName name;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @NonNull NamespaceName namespace;
  @NonNull @Setter ImmutableSet<DatasetId> inputs;
  @NonNull @Setter ImmutableSet<DatasetId> outputs;
  @Nullable URL location;
  @NonNull ImmutableMap<String, String> context;
  @Nullable String description;
  @Nullable @Setter Run latestRun;

  public Optional<URL> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<Run> getLatestRun() {
    return Optional.ofNullable(latestRun);
  }

  @JsonIgnore
  public UUID getUuid() {
    return uuid;
  }
}
