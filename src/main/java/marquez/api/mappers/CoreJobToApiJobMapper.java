package marquez.api.mappers;

import marquez.api.models.Job;

public class CoreJobToApiJobMapper extends Mapper<marquez.service.models.Job, Job> {
  @Override
  public Job map(marquez.service.models.Job value) {
    return new Job(
        value.getName(),
        value.getCreatedAt(),
        value.getInputDatasetUrns(),
        value.getOutputDatasetUrns(),
        value.getLocation(),
        value.getDescription());
  }
}
