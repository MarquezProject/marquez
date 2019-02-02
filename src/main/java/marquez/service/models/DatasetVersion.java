package marquez.service.models;

import marquez.common.models.DatasetUrn;
import marquez.common.models.NamespaceName;

public interface DatasetVersion {
  DatasetUrn toDatasetUrn(NamespaceName namespace);
}
