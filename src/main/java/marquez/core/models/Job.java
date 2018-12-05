package marquez.core.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class Job {
  private final UUID guid;
  private final String name;
  private final String description;
  private final String location;
  private final UUID namespaceGuid;
  private final Timestamp createdAt;
}
