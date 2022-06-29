/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.NotFoundException;
import marquez.common.models.Version;

public final class DatasetVersionNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public DatasetVersionNotFoundException(final Version version) {
    super(String.format("Dataset version '%s' not found.", checkNotNull(version).getValue()));
  }
}
