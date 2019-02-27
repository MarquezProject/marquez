package marquez.service.models;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class Datasource {

  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final DatasourceName dataSourceName;
  @Getter @NonNull private final ConnectionUrl connectionUrl;
}
