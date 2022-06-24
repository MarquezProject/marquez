/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.NotFoundException;
import marquez.common.models.NamespaceName;

public final class NamespaceNotFoundException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public NamespaceNotFoundException(final NamespaceName name) {
    super(String.format("Namespace '%s' not found.", checkNotNull(name).getValue()));
  }
}
