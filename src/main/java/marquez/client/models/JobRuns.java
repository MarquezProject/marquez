package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class JobRuns {
  @Getter private List<JobRun> runs;

  public JobRuns(@JsonProperty("runs") List<JobRun> runs) {
    this.runs = runs;
  }
}
