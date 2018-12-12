package marquez.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class JobRunState {

  private Timestamp transitionedAt;
  private UUID jobRunGuid;
  private String state;
}
