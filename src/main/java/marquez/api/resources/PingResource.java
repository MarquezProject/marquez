package marquez.api.resources;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/ping")
@Produces(TEXT_PLAIN)
public class PingResource {

  @GET
  @Timed
  public Response ping() {
    return Response.ok("pong").build();
  }
}
