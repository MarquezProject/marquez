package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class RunId {
  private static final int ID_LENGTH = 36;

  @Getter private final UUID value;

  private RunId(@NonNull final String value) {
    if (value.trim().isEmpty()) {
      throw new IllegalArgumentException("value must not be blank or empty");
    }
    if (value.length() != ID_LENGTH) {
      throw new IllegalArgumentException(String.format("value length must = %d", ID_LENGTH));
    }

    this.value = UUID.fromString(value);
  }

  @JsonCreator
  public static RunId fromString(String value) {
    return new RunId(value);
  }
}
