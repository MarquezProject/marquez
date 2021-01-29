package marquez.db.models;

import java.util.Optional;
import lombok.Value;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;

@Value
public class UpdateLineageResponse {
  Optional<JobInputUpdate> jobInputUpdate;
  Optional<JobOutputUpdate> jobOutputUpdate;
}
