package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ComparisonChain;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/** ID for {@code Dataset}. */
@EqualsAndHashCode
@ToString
public final class DatasetId implements Comparable<DatasetId> {
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

  @Override
  public int compareTo(DatasetId o) {
    return ComparisonChain.start()
        .compare(this.namespaceName.getValue(), o.getNamespace().getValue())
        .compare(this.getName().getValue(), o.getName().getValue())
        .result();
  }
}
