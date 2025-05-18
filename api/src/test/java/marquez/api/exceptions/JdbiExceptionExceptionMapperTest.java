/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */
package marquez.api.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.dropwizard.jersey.errors.ErrorMessage;
import jakarta.ws.rs.core.Response;
import org.jdbi.v3.core.JdbiException;
import org.junit.jupiter.api.Test;

class JdbiExceptionExceptionMapperTest {

  @Test
  void testToResponse_returnsInternalServerErrorWithErrorMessage() {
    // Arrange
    String errorMsg = "Simulated Jdbi error";
    JdbiException exception = new JdbiException(errorMsg) {};
    JdbiExceptionExceptionMapper mapper = new JdbiExceptionExceptionMapper();

    // Act
    Response response = mapper.toResponse(exception);

    // Assert
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals("application/json", response.getMediaType().toString());
    Object entity = response.getEntity();
    assertTrue(entity instanceof ErrorMessage);
    assertEquals(errorMsg, ((ErrorMessage) entity).getMessage());
  }
}
