/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class NodeIdNotFoundExceptionTest {

  @Test
  void testConstructorWithMessage() {
    String message = "Node not found";
    NodeIdNotFoundException exception = new NodeIdNotFoundException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testConstructorWithMessageAndCause() {
    String message = "Node not found";
    Throwable cause = new RuntimeException("Original error");
    NodeIdNotFoundException exception = new NodeIdNotFoundException(message, cause);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
