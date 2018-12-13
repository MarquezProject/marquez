package marquez.common.models;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Namespace {
  private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,1024}$");

  @Getter private final String value;

  public Namespace(@NonNull final String value) {
    if (!NAMESPACE_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "A namespaces must contain only letters (a-z, A-Z), numbers (0-9), or "
              + "underscores (_) with a maximum length of 1024 characters.");
    }

    this.value = value;
  }
}
