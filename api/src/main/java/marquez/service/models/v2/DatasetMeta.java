package marquez.service.models.v2;

import io.openlineage.server.OpenLineage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import marquez.common.models.NamespaceName;

@ToString
public class DatasetMeta {
  @Getter @Setter NamespaceName namespaceName;

  public DatasetMeta(@NonNull final NamespaceName namespaceName) {
    this.namespaceName = namespaceName;
  }

  public static DatasetMeta wrap(@NonNull OpenLineage.DatasetEvent datasetEvent) {
    return new DatasetMeta(NamespaceName.of(datasetEvent.getDataset().getNamespace()));
  }
}
