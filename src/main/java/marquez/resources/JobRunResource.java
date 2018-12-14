package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.util.UUID;
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
import marquez.api.CreateJobRunRequest;
import marquez.api.CreateJobRunResponse;
import marquez.api.JobRun;
import marquez.api.JobRunState;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.services.JobRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces(APPLICATION_JSON)
public class JobRunResource extends BaseResource {

  private static final Logger LOG = LoggerFactory.getLogger(JobRunResource.class);
  private final JobRunService jobRunService;

  public JobRunResource(final JobRunService jobRunService) {
    this.jobRunService = jobRunService;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Path("/namespaces/{namespace}/jobs/{job}/runs")
  @Timed
  public Response create(
      @PathParam("namespace") final String namespace,
      @PathParam("job") final String job,
      @Valid CreateJobRunRequest request)
      throws ResourceException {
    UUID jobRunGuid = UUID.randomUUID();

    // TODO: Validate that the job run definition exists. Otherwise, throw a 404.
    // This will be much easier to implement once JobRunDefinition is checked in.
    try {
      JobRun jobRun =
          new JobRun(
              jobRunGuid,
              request.getNominalStartTime(),
              request.getNominalEndTime(),
              null,
              request.getRunArgs(),
              JobRunState.State.toInt(JobRunState.State.NEW));
      jobRunService.insert(jobRun);

      CreateJobRunResponse res = new CreateJobRunResponse(jobRunGuid);
      String jsonRes = mapper.writeValueAsString(res);
      return Response.status(Response.Status.CREATED)
          .entity(jsonRes)
          .type(APPLICATION_JSON)
          .build();
    } catch (UnexpectedException | Exception e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{runId}/complete")
  public Response completeJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      jobRunService.updateJobRunState(runId, JobRunState.State.COMPLETED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{runId}/fail")
  public Response failJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      jobRunService.updateJobRunState(runId, JobRunState.State.FAILED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{runId}/abort")
  public Response abortJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      jobRunService.updateJobRunState(runId, JobRunState.State.ABORTED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{runId}/")
  public Response get(@PathParam("runId") final UUID runId) throws ResourceException {
    try {
      marquez.core.models.JobRun jobRun = jobRunService.getJobRun(runId);

      return Response.status(Response.Status.OK).entity(Entity.json(jobRun)).build();
    } catch (UnexpectedException | Exception e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }
}
