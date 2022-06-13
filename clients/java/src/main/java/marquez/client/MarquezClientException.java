/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import javax.annotation.Nullable;
import lombok.NoArgsConstructor;

/** An exception thrown to indicate a client error. */
@NoArgsConstructor
public class MarquezClientException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** Constructs a {@code MarquezClientException} with the message {@code message}. */
  MarquezClientException(@Nullable final String message) {
    super(message);
  }

  /** Constructs a {@code MarquezClientException} with the cause {@code cause}. */
  MarquezClientException(@Nullable final Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@code MarquezClientException} with the message {@code message} and the cause
   * {@code cause}.
   */
  MarquezClientException(@Nullable final String message, @Nullable final Throwable cause) {
    super(message, cause);
  }
}
