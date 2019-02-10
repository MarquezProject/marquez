package marquez.api.mappers;

import marquez.api.models.Job;

public class ApiJobToCoreJobMapper extends Mapper<Job, marquez.service.models.Job> {
  @Override
  public marquez.service.models.Job map(Job value) {
    return new marquez.service.models.Job(
        null,
        value.getName(),
        value.getLocation(),
        null,
        value.getDescription(),
        value.getInputDataSetUrns(),
        value.getOutputDataSetUrns());
  }
}
