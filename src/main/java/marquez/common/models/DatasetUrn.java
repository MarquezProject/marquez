package marquez.common.models;

import java.util.StringJoiner;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class DatasetUrn {
  private static final Integer URN_MIN_SIZE = 1;
  private static final Integer URN_MAX_SIZE = 64;
  private static final String URN_DELIM = ":";
  private static final String URN_PREFIX = "urn";
  private static final String URN_REGEX =
      String.format(
          "^%s(%s[a-zA-Z0-9.]{%d,%d}){2}$", URN_PREFIX, URN_DELIM, URN_MIN_SIZE, URN_MAX_SIZE);
  private static final Pattern URN_PATTERN = Pattern.compile(URN_REGEX);

  @Getter private final String value;

  public static DatasetUrn of(@NonNull Namespace namespace, @NonNull Dataset dataset) {
    final String value =
        new StringJoiner(URN_DELIM)
            .add(URN_PREFIX)
            .add(namespace.getValue())
            .add(dataset.getValue())
            .toString();
    return of(value);
  }

  public static DatasetUrn of(String value) {
    return new DatasetUrn(value);
  }

  private DatasetUrn(@NonNull final String value) {
    if (!URN_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "A urn must contain only letters (a-z, A-Z), numbers (0-9), and "
              + "be sperated by colons (:) with each part having a maximum length of 64 characters.");
    }

    this.value = value;
  }
}
