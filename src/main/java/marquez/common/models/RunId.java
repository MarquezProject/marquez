package marquez.common.models;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode
@ToString
public final class RunId {
  private static final int ID_LENGTH = 36;

  @Getter private final UUID value;

  public RunId(@NonNull final String value) {
    if (StringUtils.isBlank(value)) {
      throw new IllegalArgumentException("A run id must not be blank or empty.");
    }
    if (value.length() != ID_LENGTH) {
      throw new IllegalArgumentException(
          String.format("A run id must have a length of %d.", ID_LENGTH));
    }

    this.value = UUID.fromString(value);
  }
}
