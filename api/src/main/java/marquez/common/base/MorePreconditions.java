/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.base;

import static com.google.common.base.Strings.lenientFormat;

import javax.annotation.Nullable;
import lombok.NonNull;

public final class MorePreconditions {
  private MorePreconditions() {}

  public static String checkNotBlank(@NonNull final String arg) {
    if (emptyOrBlank(arg)) {
      throw new IllegalArgumentException();
    }
    return arg;
  }

  public static String checkNotBlank(
      @NonNull final String arg,
      @Nullable final String errorMessage,
      @Nullable final Object... errorMessageArgs) {
    if (emptyOrBlank(arg)) {
      throw new IllegalArgumentException(lenientFormat(errorMessage, errorMessageArgs));
    }
    return arg;
  }

  private static boolean emptyOrBlank(final String arg) {
    return arg.trim().isEmpty();
  }
}
