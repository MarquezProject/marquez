package marquez.dataset.resource;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import marquez.dataset.resource.model.Dataset;
import marquez.dataset.repository.DatasetDAO;

@Path("/datasets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DatasetResource {
  private final DatasetDAO dao;

  public DatasetResource(final DatasetDAO dao) {
    this.dao = dao;
  }

  @GET
  public List<Dataset> listDatasets() {
    return dao.findAll();
  }

  @GET
  @Path("/{dataset}/versions")
  public List<Dataset> listDatasetVersions(@PathParam("dataset") final String dataset) {
    return dao.findAll();
  }
}
