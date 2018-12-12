package marquez.api;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class JobRunState {

  private Timestamp transitionedAt;
  private UUID jobRunGuid;
  private String state;
}
