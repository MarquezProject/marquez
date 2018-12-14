package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.CoreJobRunToApiJobRunMapper;
import marquez.core.services.JobService;

@Produces(APPLICATION_JSON)
@Path("/api/v1")
@Slf4j
public class JobRunExistingResource extends BaseResource {

  private final JobService jobService;

  public JobRunExistingResource(final JobService jobService) {
    this.jobService = jobService;
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{runId}/complete")
  public Response completeJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {

      jobService.updateJobRunState(
          UUID.fromString(runId), marquez.core.models.JobRunState.State.COMPLETED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{runId}/fail")
  public Response failJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      jobService.updateJobRunState(
          UUID.fromString(runId), marquez.core.models.JobRunState.State.FAILED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("jobs/runs/{runId}/abort")
  public Response abortJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      jobService.updateJobRunState(
          UUID.fromString(runId), marquez.core.models.JobRunState.State.ABORTED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{runId}")
  public Response get(@PathParam("runId") final UUID runId) throws ResourceException {
    try {
      // TODO: Test both paths
      Optional<marquez.core.models.JobRun> jobRun = jobService.getJobRun(runId);
      if (jobRun.isPresent()) {
        return Response.status(Response.Status.OK)
            .entity(new CoreJobRunToApiJobRunMapper().map(jobRun.get()))
            .build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }
}
