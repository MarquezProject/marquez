package marquez.client.models;

import static marquez.client.Preconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Dataset {
  @Getter private final String name;
  @Getter private final Instant createdAt;
  @Getter private final String urn;
  @Getter private final String datasourceUrn;
  private final String description;

  public Dataset(
      @JsonProperty("name") final String name,
      @JsonProperty("createdAt") @NonNull final Instant createdAt,
      @JsonProperty("urn") final String urn,
      @JsonProperty("datasourceUrn") final String datasourceUrn,
      @JsonProperty("description") @Nullable final String description) {
    this.name = checkNotBlank(name);
    this.createdAt = createdAt;
    this.urn = checkNotBlank(urn);
    this.datasourceUrn = checkNotBlank(datasourceUrn);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
