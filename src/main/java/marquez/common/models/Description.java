package marquez.common.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(staticName = "of")
@EqualsAndHashCode
@ToString
public final class Description {
  private static final String NO_VALUE = "";
  public static final Description NO_DESCRIPTION = new Description(NO_VALUE);

  @Getter @NonNull private final String value;
}
