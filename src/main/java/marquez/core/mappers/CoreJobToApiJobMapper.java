package marquez.core.mappers;

import marquez.api.models.Job;

public class CoreJobToApiJobMapper extends Mapper<marquez.core.models.Job, Job> {
  @Override
  public Job map(marquez.core.models.Job value) {
    return new Job(
        value.getName(),
        value.getCreatedAt(),
        value.getInputDatasetUrns(),
        value.getOutputDatasetUrns(),
        value.getLocation(),
        value.getDescription());
  }
}
