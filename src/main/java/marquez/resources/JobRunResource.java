package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.net.URI;
import java.sql.Timestamp;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import marquez.api.*;
import marquez.db.dao.JobRunDAO;

@Path("/job_runs")
@Produces(APPLICATION_JSON)
public class JobRunResource extends BaseResource {

  private final JobRunDAO dao;

  public JobRunResource(final JobRunDAO dao) {
    this.dao = dao;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(@Valid CreateJobRunRequest request) {
    UUID jobRunGuid = UUID.randomUUID();

    try {
      JobRun jobRun =
          new JobRun(
              jobRunGuid,
              new Timestamp(System.currentTimeMillis()),
              request.getStartedAt(),
              null,
              request.getJobRunDefinitionGuid(),
              request.getCurrentState());
      dao.insert(jobRun);

      CreateJobRunResponse res = new CreateJobRunResponse(jobRunGuid);
      return Response.created(URI.create("/job_runs/" + res.getExternalGuid())).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{guid}")
  public Response get(@PathParam("guid") final UUID guid) {
    JobRun result = dao.findJobRunById(guid);
    GetJobRunResponse getJobRunResponse = new GetJobRunResponse(result.getGuid(), result.getCreatedAt(), result.getStartedAt(), result.getEndedAt(), result.getJobRunDefinitionGuid(), JobRunState.State.fromInt(result.getCurrentState()));
    try {
      String jsonRes = mapper.writeValueAsString(getJobRunResponse);

      return Response.status(Response.Status.OK).
              entity(jsonRes).
              type(APPLICATION_JSON).
              build();
    } catch (JsonProcessingException e) {
      return Response.serverError().build();
    }
  }
}
