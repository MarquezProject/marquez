package marquez.client.models;

import static marquez.client.Preconditions.checkNotBlank;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public final class DatasourceMeta {
  @Getter private final String name;
  @Getter private final String connectionUrl;

  public DatasourceMeta(final String name, final String connectionUrl) {
    this.name = checkNotBlank(name);
    this.connectionUrl = checkNotBlank(connectionUrl);
  }
}
