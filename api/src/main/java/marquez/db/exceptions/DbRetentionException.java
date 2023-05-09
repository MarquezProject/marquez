/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.exceptions;

import javax.annotation.Nullable;

public final class DbRetentionException extends DbException {
  public DbRetentionException(@Nullable String message) {
    super(message);
  }
}
