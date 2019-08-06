package marquez.client.models;

import static marquez.client.Preconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Datasource {
  @Getter private final String name;
  @Getter private final Instant createdAt;
  @Getter private final String urn;
  @Getter private final String connectionUrl;

  public Datasource(
      @JsonProperty("name") final String name,
      @JsonProperty("createdAt") @NonNull final Instant createdAt,
      @JsonProperty("urn") final String urn,
      @JsonProperty("connectionUrl") final String connectionUrl) {
    this.name = checkNotBlank(name);
    this.createdAt = createdAt;
    this.urn = checkNotBlank(urn);
    this.connectionUrl = checkNotBlank(connectionUrl);
  }
}
