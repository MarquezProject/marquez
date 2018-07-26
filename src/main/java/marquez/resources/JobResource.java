package marquez.resources;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import marquez.api.Job;
import marquez.db.JobDAO;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobResource {
  private final JobDAO dao;

  public JobResource(JobDAO dao) {
    this.dao = dao;
  }

  @GET
  public List<Job> listJobs() {
    return dao.findAll();
  }
}
