package marquez.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import marquez.api.CreateJobRunRequest;
import marquez.api.CreateJobRunResponse;
import marquez.api.GetJobRunResponse;
import marquez.api.JobRun;
import marquez.api.JobRunState;
import marquez.api.UpdateJobRunRequest;
import marquez.api.UpdateJobRunResponse;
import marquez.db.dao.JobRunDAO;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static marquez.api.JobRunState.State.fromInt;
import static marquez.api.JobRunState.State.toInt;

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

    // TODO: Validate that the job run definition exists. Otherwise, throw a 404.
    // This will be much easier to implement once JobRunDefinition is checked in.
    try {
      JobRun jobRun =
          new JobRun(
              jobRunGuid,
              new Timestamp(System.currentTimeMillis()),
              null,
              null,
              request.getJobRunDefinitionGuid(),
              request.getState());
      dao.insert(jobRun);

      CreateJobRunResponse res = new CreateJobRunResponse(jobRunGuid);
      String jsonRes = mapper.writeValueAsString(res);
      return Response.status(Response.Status.CREATED)
          .entity(jsonRes)
          .type(APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Entity.json("{'error' : 'an unexpected error occurred.'}"))
          .type(APPLICATION_JSON)
          .build();
    }
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{guid}")
  public Response update(@PathParam("guid") final UUID guid, @Valid UpdateJobRunRequest request) {
    try {
      JobRun existingJobRunRow = dao.findJobRunById(guid);
      Integer oldState = existingJobRunRow.getCurrentState();
      Integer newState;
      try {
        newState = toInt(JobRunState.State.valueOf(request.getState()));
      } catch (IllegalArgumentException iae) {
        return Response.status(Response.Status.BAD_REQUEST)
            .type(APPLICATION_JSON)
            .entity(Entity.json("'{'error': 'unknown state value'}"))
            .build();
      }

      if (!JobRun.isValidJobTransition(oldState, newState)) {
        return Response.status(Response.Status.FORBIDDEN)
            .type(APPLICATION_JSON)
            .entity(Entity.json("{'error' : 'invalid transition'}"))
            .build();
      }

      Timestamp startedAt = existingJobRunRow.getStartedAt();
      Timestamp endedAt = existingJobRunRow.getEndedAt();

      if (fromInt(oldState) == JobRunState.State.NEW
          && fromInt(newState) == JobRunState.State.RUNNING) {
        startedAt = new Timestamp(System.currentTimeMillis());
      } else if (fromInt(newState).isFinished()) {
        endedAt = new Timestamp(System.currentTimeMillis());
      }

      JobRun updatedJobRunRow =
          new JobRun(
              existingJobRunRow.getGuid(),
              existingJobRunRow.getCreatedAt(),
              startedAt,
              endedAt,
              existingJobRunRow.getJobRunDefinitionGuid(),
              toInt(JobRunState.State.valueOf(request.getState())));

      dao.update(updatedJobRunRow);

      UpdateJobRunResponse res = new UpdateJobRunResponse(guid);
      String jsonRes = mapper.writeValueAsString(res);
      return Response.status(Response.Status.ACCEPTED)
          .entity(jsonRes)
          .type(APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      System.out.println("bad");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Entity.json("{'error' : 'an unexpected error occurred.'}"))
          .type(APPLICATION_JSON)
          .build();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{guid}")
  public Response get(@PathParam("guid") final UUID guid) {
    JobRun result = dao.findJobRunById(guid);
    GetJobRunResponse getJobRunResponse =
        new GetJobRunResponse(
            result.getGuid(),
            result.getCreatedAt(),
            result.getStartedAt(),
            result.getEndedAt(),
            result.getJobRunDefinitionGuid(),
            fromInt(result.getCurrentState()));
    try {
      String jsonRes = mapper.writeValueAsString(getJobRunResponse);

      return Response.status(Response.Status.OK).entity(jsonRes).type(APPLICATION_JSON).build();
    } catch (JsonProcessingException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Entity.json("{'error' : 'an unexpected error occurred.'}"))
          .type(APPLICATION_JSON)
          .build();
    }
  }
}
