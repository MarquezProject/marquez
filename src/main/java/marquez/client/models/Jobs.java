package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Jobs {
  @Getter private List<Job> jobs;

  public Jobs(@JsonProperty("jobs") List<Job> jobs) {
    this.jobs = jobs;
  }
}
