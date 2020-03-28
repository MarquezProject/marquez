package marquez.service.models;

import lombok.NonNull;
import lombok.Value;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;

@Value
public class JobId {

  @NonNull NamespaceName namespace;
  @NonNull JobName name;
}
