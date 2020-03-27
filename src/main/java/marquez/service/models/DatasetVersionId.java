package marquez.service.models;

import java.util.UUID;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;

/** The unique ID of a dataset version in the lineage graph */
@Value
public class DatasetVersionId {
  @NonNull NamespaceName namespace;
  @NonNull DatasetName datasetName;
  @NonNull UUID version;
}
