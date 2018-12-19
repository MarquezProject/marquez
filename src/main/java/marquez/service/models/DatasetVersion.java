package marquez.service.models;

import marquez.common.models.DatasetUrn;
import marquez.common.models.Namespace;

public interface DatasetVersion {
  DatasetUrn toDatasetUrn(Namespace namespace);
}
