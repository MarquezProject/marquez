package marquez.core.mappers;

import marquez.api.Job;

public class ApiJobToCoreJobMapper extends Mapper<Job, marquez.core.models.Job> {
  @Override
  public marquez.core.models.Job map(Job value) {
    return new marquez.core.models.Job(
        null,
        value.getName(),
        value.getLocation(),
        null,
        value.getDescription(),
        value.getInputDataSetUrns(),
        value.getOutputDataSetUrns());
  }
}
