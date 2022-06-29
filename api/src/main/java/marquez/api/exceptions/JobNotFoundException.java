/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.NotFoundException;
import marquez.common.models.JobName;

public final class JobNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public JobNotFoundException(final JobName name) {
    super(String.format("Job '%s' not found.", checkNotNull(name).getValue()));
  }
}
