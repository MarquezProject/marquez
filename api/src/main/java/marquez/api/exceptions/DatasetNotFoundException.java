/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.NotFoundException;
import marquez.common.models.DatasetName;

public final class DatasetNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public DatasetNotFoundException(final DatasetName name) {
    super(String.format("Dataset '%s' not found.", checkNotNull(name).getValue()));
  }
}
