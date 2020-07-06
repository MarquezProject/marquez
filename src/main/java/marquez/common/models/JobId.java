package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/** ID for {@code Job}. */
@EqualsAndHashCode
@ToString
public final class JobId {
  @Getter private final NamespaceName namespaceName;
  @Getter private final JobName jobName;

  public JobId(
      @NonNull @JsonProperty("namespace") final NamespaceName namespaceName,
      @NonNull @JsonProperty("name") final JobName jobName) {
    this.namespaceName = namespaceName;
    this.jobName = jobName;
  }
}
