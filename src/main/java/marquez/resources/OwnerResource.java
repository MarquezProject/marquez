package marquez.resources;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import marquez.api.Owner;
import marquez.db.dao.OwnerDAO;

@Path("/owners")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OwnerResource {
  private final OwnerDAO dao;

  public OwnerResource(OwnerDAO dao) {
    this.dao = dao;
  }


}

