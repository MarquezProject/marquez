/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import java.time.Instant;
import java.util.Random;

public abstract class Generator {
  private static final Random RANDOM = new Random();

  protected static Integer newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  protected static Integer newIdWithBound(final int bound) {
    return RANDOM.nextInt(bound);
  }

  public static Instant newTimestampOrDefault(
      final boolean expression, final Instant defaultTimestamp) {
    return expression ? newTimestamp() : Instant.from(defaultTimestamp);
  }

  public static Instant newTimestamp() {
    return Instant.now();
  }

  public static String newIsoTimestamp() {
    return ISO_INSTANT.format(newTimestamp());
  }
}
