package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import marquez.api.Job;
import marquez.db.dao.JobDAO;

@Path("/jobs")
@Produces(APPLICATION_JSON)
public final class JobResource extends BaseResource {
  private static final String DEFAULT_LIMIT = "25";

  private final JobDAO dao;

  public JobResource(final JobDAO dao) {
    this.dao = dao;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(final Job job) {
    dao.insert(job);
    return Response.created(buildURI(Job.class, job.getName())).build();
  }

  @GET
  @Timed
  public Response getAll(@DefaultValue(DEFAULT_LIMIT) @QueryParam("limit") final int limit) {
    return Response.ok(dao.findAll(limit)).build();
  }

  @GET
  @Path("/{name}")
  @Timed
  public Response getJob(@PathParam("name") final String name) {
    return Response.ok(dao.findByName(name)).build();
  }

  @PUT
  @Path("/{name}")
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response update(@PathParam("name") final String jobName) {
    return Response.ok().build();
  }
}
