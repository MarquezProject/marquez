package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import marquez.api.CreateJobRunRequest;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.CoreJobRunToApiJobRunMapper;
import marquez.core.models.JobRun;
import marquez.core.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces(APPLICATION_JSON)
public class JobRunResource extends BaseResource {

  private static final Logger LOG = LoggerFactory.getLogger(JobRunResource.class);
  private final JobService jobService;

  public JobRunResource(final JobService jobService) {
    this.jobService = jobService;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Path("/api/v1/namespaces/{namespace}/jobs/{job}/runs")
  // @Timed
  public Response create(
      @PathParam("namespace") final String namespace,
      @PathParam("job") final String job,
      @Valid CreateJobRunRequest request)
      throws ResourceException {
    try {
      JobRun createdJobRun =
          jobService.createJobRun(
              namespace,
              job,
              request.getRunArgs(),
              request.getNominalStartTime(),
              request.getNominalEndTime());
      return Response.status(Response.Status.CREATED)
          .entity(new CoreJobRunToApiJobRunMapper().map(createdJobRun))
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
  @Path("/api/v1/jobs/runs/{runId}/complete")
  public Response completeJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {

      jobService.updateJobRunState(
          UUID.fromString(runId), marquez.core.models.JobRunState.State.COMPLETED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/api/v1/jobs/runs/{runId}/fail")
  public Response failJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      jobService.updateJobRunState(
          UUID.fromString(runId), marquez.core.models.JobRunState.State.FAILED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/api/v1/jobs/runs/{runId}/abort")
  public Response abortJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      jobService.updateJobRunState(
          UUID.fromString(runId), marquez.core.models.JobRunState.State.ABORTED);
      return Response.status(Response.Status.OK).build();
    } catch (UnexpectedException | Exception e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/api/v1/jobs/runs/{runId}/")
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
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }
}
