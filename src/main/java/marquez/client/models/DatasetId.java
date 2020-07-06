package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/** ID for {@link Dataset}. */
@EqualsAndHashCode
@ToString
public final class DatasetId {
  private final String namespace;
  private final String name;

  public DatasetId(@NonNull final String namespace, @NonNull final String name) {
    this.namespace = namespace;
    this.name = name;
  }

  @JsonProperty
  public String namespace() {
    return namespace;
  }

  @JsonProperty
  public String name() {
    return name;
  }
}
