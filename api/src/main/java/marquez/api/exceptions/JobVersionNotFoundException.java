/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.NotFoundException;
import marquez.common.models.Version;

public final class JobVersionNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public JobVersionNotFoundException(final Version version) {
    super(String.format("Job version '%s' not found.", checkNotNull(version).getValue()));
  }
}
