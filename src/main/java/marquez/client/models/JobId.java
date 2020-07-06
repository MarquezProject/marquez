package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/** ID for {@link Job}. */
@EqualsAndHashCode
@ToString
public final class JobId {
  @Getter private final String namespaceName;
  @Getter private final String jobName;

  public JobId(
      @NonNull @JsonProperty("namespace") final String namespaceName,
      @NonNull @JsonProperty("name") final String jobName) {
    this.namespaceName = namespaceName;
    this.jobName = jobName;
  }
}
