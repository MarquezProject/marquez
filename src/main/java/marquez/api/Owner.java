package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class Owner {
  @NotNull private final String name;

  @JsonCreator
  public Owner(@JsonProperty("name") final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Owner)) return false;

    final Owner other = (Owner) o;

    return Objects.equals(name, other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Owner{");
    sb.append("name=").append(name);
    sb.append("}");
    return sb.toString();
  }
}
