package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/** ID for {@code Dataset}. */
@EqualsAndHashCode
@ToString
public final class DatasetId {
  @Getter private final NamespaceName namespaceName;
  @Getter private final DatasetName datasetName;

  public DatasetId(
      @NonNull @JsonProperty("namespace") final NamespaceName namespaceName,
      @NonNull @JsonProperty("name") final DatasetName datasetName) {
    this.namespaceName = namespaceName;
    this.datasetName = datasetName;
  }
}
