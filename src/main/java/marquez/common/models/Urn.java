package marquez.common.models;

import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NonNull;

public class Urn {
  private static final String URN_DELIM = ":";
  private static final String URN_PREFIX = "urn";
  private static final Integer URN_MIN_SIZE = 1;
  private static final Integer URN_MAX_SIZE = 64;

  private static final String VALID_CHAR_CLASS = "a-zA-Z0-9_.";
  private static final String INVALID_CHAR_CLASS =
      "[^" + VALID_CHAR_CLASS + ":]"; // ':' is intrinsic to URNs, so added here
  private static final String REGEX_PATTERN = "%s:%s(%s[" + VALID_CHAR_CLASS + "]{%d,%d})";

  private static final String VALIDATION_ERROR =
      "A urn must contain only letters (a-z, A-Z), numbers (0-9), and "
          + "be sperated by colons (:) with each part having a maximum length of 64 characters.";

  @Getter private final String value;

  protected static String fromComponents(String urnType, String... components) {
    String rawUrn =
        String.format("%s:%s:%s", URN_PREFIX, urnType, String.join(URN_DELIM, components));
    return rawUrn.replaceAll(INVALID_CHAR_CLASS, "");
  }

  protected static Pattern buildPattern(String urnType, int numComponents) {
    String regex =
        String.format(
            "^" + REGEX_PATTERN + "{%d}$",
            URN_PREFIX,
            urnType,
            URN_DELIM,
            URN_MIN_SIZE,
            URN_MAX_SIZE,
            numComponents);
    return Pattern.compile(regex);
  }

  protected Urn(@NonNull final String value, Pattern pattern) {
    if (!pattern.matcher(value).matches()) {
      throw new IllegalArgumentException(
          String.format("Invalid URN '%s'. %s", value, VALIDATION_ERROR));
    }
    this.value = value;
  }

  public String toString() {
    return this.value;
  }
}
