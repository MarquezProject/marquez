package marquez.core.mappers;

import java.util.Collections;
import marquez.api.Job;

public class CoreJobToApiJobMapper extends Mapper<marquez.core.models.Job, marquez.api.Job> {
  @Override
  public Job map(marquez.core.models.Job value) {
    // TODO: Update to add real values for input and output datasets
    return new marquez.api.Job(
        value.getName(),
        value.getCreatedAt(),
        Collections.emptyList(),
        Collections.emptyList(),
        value.getLocation(),
        value.getDescription());
  }
}
