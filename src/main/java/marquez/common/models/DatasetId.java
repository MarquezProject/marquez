package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/** ID for {@code Dataset}. */
@EqualsAndHashCode
@ToString
public final class DatasetId {
  private final NamespaceName namespaceName;
  private final DatasetName datasetName;

  public DatasetId(
      @JsonProperty("namespace") @NonNull NamespaceName namespaceName,
      @JsonProperty("name") @NonNull DatasetName datasetName) {
    this.namespaceName = namespaceName;
    this.datasetName = datasetName;
  }

  public NamespaceName getNamespace() {
    return namespaceName;
  }

  public DatasetName getName() {
    return datasetName;
  }
}
