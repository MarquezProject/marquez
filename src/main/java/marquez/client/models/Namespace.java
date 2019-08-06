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
public final class Namespace {
  @Getter private final String name;
  @Getter private final Instant createdAt;
  @Getter private final String ownerName;
  private final String description;

  public Namespace(
      @JsonProperty("name") final String name,
      @JsonProperty("createdAt") @NonNull final Instant createdAt,
      @JsonProperty("ownerName") final String ownerName,
      @JsonProperty("description") @Nullable final String description) {
    this.name = checkNotBlank(name);
    this.createdAt = createdAt;
    this.ownerName = checkNotBlank(ownerName);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
