package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static marquez.resources.ResourceUtil.buildLocation;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import marquez.api.Owner;
import marquez.db.dao.OwnerDAO;

@Path("/owners")
public class OwnerResource {
  private final OwnerDAO ownerDAO;

  public OwnerResource(final OwnerDAO ownerDAO) {
    this.ownerDAO = ownerDAO;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(final Owner owner) {
    ownerDAO.insert(owner);
    return Response.created(buildLocation(Owner.class, owner.getName())).build();
  }
}
