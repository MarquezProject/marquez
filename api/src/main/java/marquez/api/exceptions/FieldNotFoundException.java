/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.NotFoundException;
import marquez.common.models.DatasetName;
import marquez.common.models.FieldName;

public final class FieldNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public FieldNotFoundException(final DatasetName datasetName, final FieldName fieldName) {
    super(
        String.format(
            "Field '%s' not found for dataset '%s'.",
            checkNotNull(fieldName.getValue()), checkNotNull(datasetName).getValue()));
  }
}
