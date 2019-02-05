package marquez.common;

import javax.annotation.Nullable;

public final class Preconditions {
  private Preconditions() {}

  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public static <T> T checkNotNull(T reference, @Nullable String errorMessage) {
    if (reference == null) {
      throw new NullPointerException(errorMessage);
    }
    return reference;
  }

  public static String checkNotBlank(String argument) {
    checkNotNull(argument);
    if (argument.trim().isEmpty()) {
      throw new IllegalArgumentException();
    }
    return argument;
  }

  public static String checkNotBlank(String argument, @Nullable String errorMessage) {
    checkNotNull(argument);
    if (argument.trim().isEmpty()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return argument;
  }

  public static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  public static void checkArgument(boolean expression, @Nullable String errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(errorMessage);
    }
  }
}
