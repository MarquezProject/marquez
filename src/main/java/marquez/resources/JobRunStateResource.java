package marquez.resources;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Timestamp;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.*;
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
          new Timestamp(System.currentTimeMillis()),
          request.getJobRunGuid(),
          JobRunState.State.toInt(request.getState()));

      CreateJobRunStateResponse res = new CreateJobRunStateResponse(jobRunStateGuid);
      String jsonRes = mapper.writeValueAsString(res);
      return Response.status(HTTP_CREATED)
          .header("Location", "/job_run_states/" + jobRunStateGuid)
          .entity(jsonRes)
          .type(APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{guid}")
  public Response get(@PathParam("guid") final UUID guid) {

    JobRunState result = dao.findJobRunStateById(guid);
    GetJobRunStateResponse getJobRunStateResponse =
        new GetJobRunStateResponse(
            result.getGuid(),
            result.getTransitionedAt(),
            result.getJobRunGuid(),
            result.getState());
    try {
      String jsonRes = mapper.writeValueAsString(getJobRunStateResponse);

      return Response.status(Response.Status.OK).entity(jsonRes).type(APPLICATION_JSON).build();
    } catch (JsonProcessingException e) {
      return Response.serverError().build();
    }
  }
}
