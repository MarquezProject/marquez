package marquez.core.mappers;

import java.util.Collections;
import marquez.api.models.Job;

public class CoreJobToApiJobMapper extends Mapper<marquez.core.models.Job, Job> {
  @Override
  public Job map(marquez.core.models.Job value) {
    return new Job(
        value.getName(),
        value.getCreatedAt(),
        value.getInputDatasetUrns().isEmpty()
            ? Collections.emptyList()
            : value.getInputDatasetUrns(),
        value.getOutputDatasetUrns().isEmpty()
            ? Collections.emptyList()
            : value.getOutputDatasetUrns(),
        value.getLocation(),
        value.getDescription());
  }
}
