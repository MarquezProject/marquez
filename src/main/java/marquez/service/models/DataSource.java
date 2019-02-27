package marquez.service.models;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSourceName;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class DataSource {

  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final DataSourceName dataSourceName;
  @Getter @NonNull private final ConnectionUrl connectionUrl;
}
