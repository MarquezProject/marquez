package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.util.List;
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
import lombok.extern.slf4j.Slf4j;
import marquez.api.CreateJobRequest;
import marquez.api.CreateJobRunRequest;
import marquez.api.ListJobsResponse;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.ApiJobToCoreJobMapper;
import marquez.core.mappers.CoreJobRunToApiJobRunMapper;
import marquez.core.mappers.CoreJobToApiJobMapper;
import marquez.core.models.Job;
import marquez.core.models.JobRun;
import marquez.core.services.JobService;
import marquez.core.services.NamespaceService;

@Path("/api/v1/namespaces/{namespace}/jobs")
@Produces(APPLICATION_JSON)
@Slf4j
public final class JobResource extends BaseResource {
  private final JobService jobService;
  private final NamespaceService namespaceService;

  private ApiJobToCoreJobMapper apiJobToCoreJobMapper = new ApiJobToCoreJobMapper();
  private CoreJobToApiJobMapper coreJobToApiJobMapper = new CoreJobToApiJobMapper();

  public JobResource(final NamespaceService namespaceService, final JobService jobService) {
    this.namespaceService = namespaceService;
    this.jobService = jobService;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Path("/namespaces/{namespace}/jobs/{job}/runs")
  public Response create(
      @PathParam("namespace") final String namespace,
      @PathParam("job") final String job,
      CreateJobRunRequest request)
      throws ResourceException {
    // TODO: Verify that the job exists
    try {
      if (!namespaceExists(namespace)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      if (!jobService.getJob(namespace, job).isPresent()) {
        log.error("Could not find job: " + job);
        return Response.status(Response.Status.NOT_FOUND).build();
      }
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
      log.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @PUT
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(
      @PathParam("namespace") final String namespace,
      @PathParam("job") final String job,
      @Valid CreateJobRequest request)
      throws ResourceException {
    try {
      if (!jobService.getJob(namespace, job).isPresent()) {
        log.error("Could not find job: " + job);
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      Job jobToCreate =
          apiJobToCoreJobMapper.map(
              new marquez.api.Job(
                  job,
                  null,
                  request.getInputDataSetUrns(),
                  request.getOutputDatasetUrns(),
                  request.getLocation(),
                  request.getDescription()));
      jobToCreate.setNamespaceGuid(namespaceService.get(namespace).get().getGuid());
      Job createdJob = jobService.createJob(namespace, jobToCreate);
      return Response.status(Response.Status.CREATED)
          .entity(coreJobToApiJobMapper.map(createdJob))
          .build();
    } catch (UnexpectedException e) {
      log.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Timed
  public Response getJob(
      @PathParam("namespace") final String namespace, @PathParam("job") final String job)
      throws ResourceException {

    // TODO: Verify that the job exists
    try {
      Optional<Job> returnedJob = jobService.getJob(namespace, job);
      if (returnedJob.isPresent()) {
        return Response.status(Response.Status.OK)
            .entity(coreJobToApiJobMapper.map(returnedJob.get()))
            .build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (UnexpectedException e) {
      log.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Timed
  @Path("/namespaces")
  public Response listJobs(@PathParam("namespace") final String namespace)
      throws ResourceException {
    // TODO: Deal with the case of an invalid namespace
    try {
      if (!namespaceExists(namespace)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      List<Job> jobList = jobService.getAllJobsInNamespace(namespace);
      return Response.status(Response.Status.OK).entity(new ListJobsResponse(jobList)).build();

    } catch (UnexpectedException e) {
      log.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
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
  @Path("/jobs/runs/{runId}/abort")
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

  public boolean namespaceExists(String namespace) throws UnexpectedException {
    return namespaceService.get(namespace).isPresent();
  }
}
