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
public final class DatasetMeta {
  @Getter private final String name;
  @Getter private final String datasourceUrn;
  private final String description;

  public DatasetMeta(
      final String name, final String datasourceUrn, @Nullable final String description) {
    this.name = checkNotBlank(name);
    this.datasourceUrn = checkNotBlank(datasourceUrn);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
