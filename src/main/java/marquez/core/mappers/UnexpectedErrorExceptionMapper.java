package marquez.core.mappers;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import marquez.api.entities.ErrorResponse;
import marquez.core.exceptions.UnexpectedErrorException;

@Provider
public class UnexpectedErrorExceptionMapper implements ExceptionMapper<UnexpectedErrorException> {

  @Override
  public Response toResponse(UnexpectedErrorException exception) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(new ErrorResponse("Unexpected error occurred"))
        .type(APPLICATION_JSON)
        .build();
  }
}
