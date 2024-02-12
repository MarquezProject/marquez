package marquez.service.models.v2;

import io.openlineage.server.OpenLineage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import marquez.common.models.NamespaceName;

@ToString
public class RunMeta {
  @Getter @Setter NamespaceName namespaceName;

  public RunMeta(@NonNull final NamespaceName namespaceName) {
    this.namespaceName = namespaceName;
  }

  public static RunMeta newInstance(@NonNull OpenLineage.RunEvent runEvent) {
    return new RunMeta(NamespaceName.of(runEvent.getJob().getNamespace()));
  }
}
