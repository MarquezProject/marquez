package marquez.service.models.v2;

import io.openlineage.server.OpenLineage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import marquez.common.models.NamespaceName;

@ToString
public class JobMeta {
  @Getter @Setter NamespaceName namespaceName;

  public JobMeta(@NonNull final NamespaceName namespaceName) {
    this.namespaceName = namespaceName;
  }

  public static JobMeta wrap(@NonNull OpenLineage.JobEvent jobEvent) {
    return new JobMeta(NamespaceName.of(jobEvent.getJob().getNamespace()));
  }
}
