package marquez.common.models;

import static marquez.common.Preconditions.checkArgument;
import static marquez.common.Preconditions.checkNotBlank;

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

  public RunId(@NonNull final String value) {
    checkNotBlank(value, "value must not be blank or empty");
    checkArgument(value.length() == ID_LENGTH, String.format("value length must = %d", ID_LENGTH));
    this.value = UUID.fromString(value);
  }
}
