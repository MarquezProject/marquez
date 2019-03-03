package marquez.api.exceptions;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import marquez.service.exceptions.MarquezServiceException;

public final class MarquezServiceExceptionMapper
    implements ExceptionMapper<MarquezServiceException> {
  @Override
  public Response toResponse(MarquezServiceException e) {
    return Response.status(INTERNAL_SERVER_ERROR)
        .type(APPLICATION_JSON_TYPE)
        .entity(new ErrorMessage(INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage()))
        .build();
  }
}
