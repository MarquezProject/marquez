/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.exceptions;

import javax.annotation.Nullable;

/** An exception thrown to indicate a database error. */
public class DbException extends Exception {
  private static final long serialVersionUID = 1L;

  /** Constructs a {@code DbException} with the provided {@code message}. */
  public DbException(@Nullable final String message) {
    super(message);
  }

  /** Constructs a {@code DbException} with the provided {@code cause}. */
  DbException(@Nullable final Throwable cause) {
    super(cause);
  }

  /** Constructs a {@code DbException} with the provided {@code message} and the {@code cause}. */
  DbException(@Nullable final String message, @Nullable final Throwable cause) {
    super(message, cause);
  }
}
