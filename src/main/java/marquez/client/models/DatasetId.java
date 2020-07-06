package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/** ID for {@link Dataset}. */
@EqualsAndHashCode
@ToString
public final class DatasetId {
  @Getter private final String namespaceName;
  @Getter private final String datasetName;

  public DatasetId(
      @NonNull @JsonProperty("namespace") final String namespaceName,
      @NonNull @JsonProperty("name") final String datasetName) {
    this.namespaceName = namespaceName;
    this.datasetName = datasetName;
  }
}
