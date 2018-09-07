package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.net.URI;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import marquez.api.*;
import marquez.db.dao.JobRunStateDAO;

@Path("/job_run_states")
public class JobRunStateResource extends BaseResource {
  private final JobRunStateDAO dao;

  public JobRunStateResource(final JobRunStateDAO dao) {
    this.dao = dao;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(@Valid CreateJobRunStateRequest request) {
    UUID jobRunStateGuid = UUID.randomUUID();
    try {
      dao.insert(
          jobRunStateGuid,
          request.getJobRunGuid(),
          request.getTransitionedAt(),
          JobRunState.State.toInt(request.getState()));

      CreateJobRunStateResponse res = new CreateJobRunStateResponse(jobRunStateGuid);
      return Response.created(URI.create("/job_run_states/" + res.getExternalGuid())).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }
}
