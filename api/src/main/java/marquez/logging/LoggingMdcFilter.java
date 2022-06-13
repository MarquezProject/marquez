/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.logging;

import java.io.IOException;
import java.util.UUID;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Puts request ID, method, and path to the MDC context so that this information is available from
 * logs throughout the request context. Request ID is randomly generated UUID, which can be used to
 * group by to see the logs for a particular request.
 */
@Slf4j
public class LoggingMdcFilter
    implements ContainerRequestFilter, ContainerResponseFilter, CompletionCallback {

  private static final String REQUEST_ID = "requestID";
  private static final String METHOD = "method";
  private static final String PATH = "path";

  @Override
  public void onComplete(Throwable throwable) {
    MDC.remove(REQUEST_ID);
    MDC.remove(METHOD);
    MDC.remove(PATH);
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    MDC.put(REQUEST_ID, UUID.randomUUID().toString());
    MDC.put(METHOD, requestContext.getMethod());
    MDC.put(PATH, requestContext.getUriInfo().getPath());
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    if (responseContext.getStatus() >= Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
      log.error("status: {}", responseContext.getStatus());
    } else if (responseContext.getStatus() >= Response.Status.BAD_REQUEST.getStatusCode()) {
      log.warn("status: {}", responseContext.getStatus());
    } else {
      log.info("status: {}", responseContext.getStatus());
    }
  }
}
