package marquez.common.models;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class Urn {
  private static final Pattern URN_PATTERN = Pattern.compile("^urn(:[a-zA-Z0-9]{1,64}){3}$");

  @Getter private final String value;

  public Urn(final String value) {
    requireNonNull(value, "value must not be null");

    if (!URN_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "A urn must contain only letters (a-z, A-Z), numbers (0-9), and "
              + "be sperated by colons (:) with each part having a maximum length of 64 characters.");
    }

    this.value = value;
  }
}
