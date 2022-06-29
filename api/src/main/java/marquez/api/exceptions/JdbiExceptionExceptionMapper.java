/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.exceptions;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import io.dropwizard.jersey.errors.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.JdbiException;

@Slf4j
public class JdbiExceptionExceptionMapper implements ExceptionMapper<JdbiException> {

  @Override
  public Response toResponse(JdbiException e) {
    log.error("Failed to execute statement", e);
    return Response.serverError()
        .type(APPLICATION_JSON_TYPE)
        .entity(new ErrorMessage(INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage()))
        .build();
  }
}
