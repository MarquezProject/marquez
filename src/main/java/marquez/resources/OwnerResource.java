package marquez.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import marquez.api.Owner;
import marquez.db.dao.OwnerDAO;

@Path("/owners")
public class OwnerResource {
  private final OwnerDAO dao;

  public OwnerResource(final OwnerDAO dao) {
    this.dao = dao;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void createOwner(final Owner owner) {
    dao.insert(owner);
  }
}
