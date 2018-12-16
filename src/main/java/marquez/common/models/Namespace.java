package marquez.common.models;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Namespace {
  private static final Integer MIN_SIZE = 1;
  private static final Integer MAX_SIZE = 1024;
  private static final Pattern PATTERN =
      Pattern.compile(String.format("^[a-zA-Z0-9_]{%d,%d}$", MIN_SIZE, MAX_SIZE));

  @Getter private final String value;

  public static Namespace of(String value) {
    return new Namespace(value);
  }

  private Namespace(@NonNull final String value) {
    if (!PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "A namespaces must contain only letters (a-z, A-Z), numbers (0-9), or "
              + "underscores (_) with a maximum length of 1024 characters.");
    }

    this.value = value;
  }
}
