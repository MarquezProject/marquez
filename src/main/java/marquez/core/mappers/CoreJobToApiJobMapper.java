package marquez.core.mappers;

import java.util.Collections;
import marquez.api.models.Job;

public class CoreJobToApiJobMapper extends Mapper<marquez.core.models.Job, Job> {
  @Override
  public Job map(marquez.core.models.Job value) {
    // TODO: Update to add real values for input and output datasets
    return new Job(
        value.getName(),
        value.getCreatedAt(),
        Collections.emptyList(),
        Collections.emptyList(),
        value.getLocation(),
        value.getDescription());
  }
}
