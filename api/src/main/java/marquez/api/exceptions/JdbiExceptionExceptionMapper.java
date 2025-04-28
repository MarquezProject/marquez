/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import io.dropwizard.jersey.errors.ErrorMessage;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.JdbiException;

@Slf4j
public class JdbiExceptionExceptionMapper implements ExceptionMapper<JdbiException> {

  @Override
  public Response toResponse(JdbiException e) {
    log.error("Failed to execute statement", e);
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(new ErrorMessage(e.getMessage()))
        .build();
  }
}
