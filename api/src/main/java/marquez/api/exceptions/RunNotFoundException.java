/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.NotFoundException;
import marquez.common.models.RunId;

public final class RunNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public RunNotFoundException(final RunId id) {
    super(String.format("Run '%s' not found.", checkNotNull(id).getValue()));
  }
}
