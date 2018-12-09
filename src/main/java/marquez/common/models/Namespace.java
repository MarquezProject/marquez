package marquez.common.models;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class Namespace {
  private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,1024}$");

  private final String value;

  public Namespace(final String value) {
    requireNonNull(value, "value must not be null");

    if (!NAMESPACE_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "A namespaces must contain only letters (a-z, A-Z), numbers (0-9), or "
              + "underscores (_) with a maximum length of 1024 characters.");
    }

    this.value = value;
  }
}
