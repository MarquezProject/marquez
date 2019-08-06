package marquez.client.models;

import static marquez.client.Preconditions.checkNotBlank;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public final class NamespaceMeta {
  @Getter private final String ownerName;
  private final String description;

  public NamespaceMeta(final String ownerName, @Nullable final String description) {
    this.ownerName = checkNotBlank(ownerName);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
