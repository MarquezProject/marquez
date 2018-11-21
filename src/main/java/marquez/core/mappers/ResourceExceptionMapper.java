package marquez.core.mappers;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import marquez.api.entities.ErrorResponse;
import marquez.core.exceptions.ResourceException;

@Provider
public class ResourceExceptionMapper implements ExceptionMapper<ResourceException> {

  @Override
  public Response toResponse(ResourceException exception) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(new ErrorResponse("Resource exception occurred"))
        .type(APPLICATION_JSON)
        .build();
  }
}
