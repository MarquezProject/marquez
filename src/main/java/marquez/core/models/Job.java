package marquez.core.models;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public final class Job {
  @NonNull private final UUID guid;
  @NonNull private final String name;
  @NonNull private final String location;
  @NonNull private final UUID namespaceGuid;
  private final String description;
  private final List<String> inputDatasetUrns;
  private final List<String> outputDatasetUrns;
  private final Timestamp createdAt;

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
