package marquez.service.models;

import java.util.UUID;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;

/** The unique identifier of a Job Version in the lineage graph */
@Value
public class JobVersionId {
  @NonNull NamespaceName namespace;
  @NonNull JobName jobName;
  @NonNull UUID version;
}
