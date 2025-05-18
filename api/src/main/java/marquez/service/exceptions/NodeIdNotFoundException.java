/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.exceptions;

public class NodeIdNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public NodeIdNotFoundException(String message) {
    super(message);
  }

  public NodeIdNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
