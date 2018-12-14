package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import marquez.core.mappers.CoreJobToApiJobMapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListJobsResponse {

  private static final CoreJobToApiJobMapper jobMapper = new CoreJobToApiJobMapper();

  private List<marquez.core.models.Job> jobs;

  @JsonProperty("jobs")
  public List<marquez.api.Job> getJobs() {
    return jobMapper.map(jobs);
  }
}
