/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.BadRequestException;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;

public final class RunAlreadyExistsException extends BadRequestException {
  private static final long serialVersionUID = 1L;

  public RunAlreadyExistsException(
      final NamespaceName namespaceName, final JobName jobName, final RunId id) {
    super(
        String.format(
            "Run '%s' already exists for '%s' under namespace '%s'.",
            checkNotNull(id).getValue(),
            checkNotNull(jobName).getValue(),
            checkNotNull(namespaceName).getValue()));
  }
}
