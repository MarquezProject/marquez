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
  private static final int URN_MIN_SIZE = 1;
  private static final int URN_MAX_SIZE = 64;
  private static final String URN_DELIM = ":";
  private static final String URN_PREFIX = "urn";
  private static final String URN_REGEX =
      String.format(
          "^%s(%s[a-zA-Z0-9._]{%d,%d}){2}$", URN_PREFIX, URN_DELIM, URN_MIN_SIZE, URN_MAX_SIZE);
  private static final Pattern URN_PATTERN = Pattern.compile(URN_REGEX);

  @Getter private final String value;

  private DatasetUrn(@NonNull final String value) {
    if (!URN_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "A urn must contain only letters (a-z, A-Z), numbers (0-9), or underscores (_) and "
              + "be sperated by colons (:) with each part having a maximum length of 64 characters.");
    }

    this.value = value;
  }

  public static DatasetUrn from(
      @NonNull NamespaceName namespaceName, @NonNull DatasetName datasetName) {
    final String value =
        new StringJoiner(URN_DELIM)
            .add(URN_PREFIX)
            .add(namespaceName.getValue())
            .add(datasetName.getValue())
            .toString();
    return fromString(value);
  }

  public static DatasetUrn fromString(String value) {
    return new DatasetUrn(value);
  }
}
