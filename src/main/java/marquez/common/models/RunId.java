package marquez.common.models;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

@EqualsAndHashCode
@ToString
public final class RunId {
  private static final int ID_LENGTH = 36;

  @Getter private final UUID value;

  public RunId(@NonNull final String value) {
    Validate.notBlank(value, "value must not be blank or empty");
    Validate.isTrue(
        value.length() == ID_LENGTH, "value length must = %d", ID_LENGTH);

    this.value = UUID.fromString(value);
  }
}
