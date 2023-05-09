/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.exceptions;

import javax.annotation.Nullable;

public class DbException extends Exception {
  private static final long serialVersionUID = 1L;

  public DbException(@Nullable final String message) {
    super(message);
  }
}
