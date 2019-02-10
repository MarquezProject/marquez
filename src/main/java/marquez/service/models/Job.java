package marquez.service.models;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
public final class Job {
  @NonNull @Getter private final UUID guid;
  @NonNull @Getter private final String name;
  @NonNull @Getter private final String location;
  @NonNull @Getter @Setter private UUID namespaceGuid;
  @Getter private final String description;
  @Getter private final List<String> inputDatasetUrns;
  @Getter private final List<String> outputDatasetUrns;
  @Getter private final Timestamp createdAt;

  public Job(
      final UUID guid,
      final String name,
      final String location,
      final UUID namespaceGuid,
      final String description,
      final List<String> inputDatasetUrns,
      final List<String> outputDatasetUrns) {
    this.guid = guid;
    this.name = name;
    this.location = location;
    this.namespaceGuid = namespaceGuid;
    this.description = description;
    this.inputDatasetUrns = inputDatasetUrns;
    this.outputDatasetUrns = outputDatasetUrns;
    this.createdAt = null;
  }
}
