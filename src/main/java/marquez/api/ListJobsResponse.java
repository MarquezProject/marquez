package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import marquez.core.mappers.CoreJobToApiJobMapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListJobsResponse {

  private static final CoreJobToApiJobMapper jobMapper = new CoreJobToApiJobMapper();

  @JsonProperty("jobs")
  @Getter(AccessLevel.NONE)
  private List<marquez.core.models.Job> jobs;

  @JsonProperty("jobs")
  public List<marquez.api.Job> getJobs() {
    // TODO: Update this to use the right mapper
    return jobs.stream().map(jobMapper::map).collect(Collectors.toList());
  }
}
