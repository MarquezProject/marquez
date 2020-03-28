package marquez.service.models;

import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;

@Value
public class DatasetId {

  @NonNull NamespaceName namespace;
  @NonNull DatasetName name;
}
