package marquez.resources;

import static marquez.resources.ResourceUtil.buildLocation;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.api.Job;
import marquez.db.dao.JobDAO;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
public class JobResource {
  private final JobDAO jobDAO;

  public JobResource(final JobDAO jobDAO) {
    this.jobDAO = jobDAO;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Timed
  public Response create(final Job job) {
    jobDAO.insert(job);
    return Response.created(buildLocation(Job.class, job.getName())).build();
  }

  @GET
  @Timed
  public Response getAll() {
    return Response.ok(jobDAO.findAll()).build();
  }

  @GET
  @Path("/{jobName}")
  @Timed
  public Response get(@PathParam("jobName") final String jobName) {
    return Response.ok(jobDAO.findByName(jobName)).build();
  }

  @PUT
  @Path("/{jobName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Timed
  public Response update(@PathParam("jobName") final String jobName) {
    return Response.ok().build();
  }
}
