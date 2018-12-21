package marquez.api.resources;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.sql.Timestamp;
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
import marquez.api.models.CreateJobRequest;
import marquez.api.models.CreateJobRunRequest;
import marquez.api.models.JobsResponse;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.ApiJobToCoreJobMapper;
import marquez.core.mappers.CoreJobRunToApiJobRunResponseMapper;
import marquez.core.mappers.CoreJobToApiJobMapper;
import marquez.core.models.Job;
import marquez.core.models.JobRun;
import marquez.core.models.JobRunState;
import marquez.core.services.JobService;
import marquez.core.services.NamespaceService;

@Path("/api/v1")
@Slf4j
public final class JobResource {
  private final JobService jobService;
  private final NamespaceService namespaceService;

  private final ApiJobToCoreJobMapper apiJobToCoreJobMapper = new ApiJobToCoreJobMapper();
  private final CoreJobToApiJobMapper coreJobToApiJobMapper = new CoreJobToApiJobMapper();
  private final CoreJobRunToApiJobRunResponseMapper coreJobRunToApiJobRunMapper =
      new CoreJobRunToApiJobRunResponseMapper();

  public JobResource(final NamespaceService namespaceService, final JobService jobService) {
    this.namespaceService = namespaceService;
    this.jobService = jobService;
  }

  @PUT
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @Timed
  public Response create(
      @PathParam("namespace") final String namespace,
      @PathParam("job") final String job,
      @Valid final CreateJobRequest request)
      throws ResourceException {
    try {
      if (!namespaceService.exists(namespace)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      final Job jobToCreate =
          apiJobToCoreJobMapper.map(
              new marquez.api.models.Job(
                  job,
                  null,
                  request.getInputDataSetUrns(),
                  request.getOutputDatasetUrns(),
                  request.getLocation(),
                  request.getDescription()));
      jobToCreate.setNamespaceGuid(namespaceService.get(namespace).get().getGuid());
      final Job createdJob = jobService.createJob(namespace, jobToCreate);
      return Response.status(Response.Status.CREATED)
          .entity(coreJobToApiJobMapper.map(createdJob))
          .build();
    } catch (UnexpectedException e) {
      log.error(format("Error creating the job <%s>:<%s>.", namespace, job), e);
      throw new ResourceException();
    }
  }

  @GET
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Produces(APPLICATION_JSON)
  @Timed
  public Response getJob(
      @PathParam("namespace") final String namespace, @PathParam("job") final String job)
      throws ResourceException {
    try {
      if (!namespaceService.exists(namespace)) {
        return Response.status(Response.Status.NOT_FOUND).entity("Namespace not found").build();
      }
      final Optional<Job> returnedJob = jobService.getJob(namespace, job);
      if (returnedJob.isPresent()) {
        return Response.ok().entity(coreJobToApiJobMapper.map(returnedJob.get())).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (UnexpectedException e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @Path("/namespaces/{namespace}/jobs")
  public Response listJobs(@PathParam("namespace") final String namespace)
      throws ResourceException {
    try {
      if (!namespaceService.exists(namespace)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      final List<Job> jobList = jobService.getAllJobsInNamespace(namespace);
      final JobsResponse response = new JobsResponse(coreJobToApiJobMapper.map(jobList));
      return Response.ok().entity(response).build();
    } catch (UnexpectedException e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @POST
  @Produces(APPLICATION_JSON)
  @Consumes(APPLICATION_JSON)
  @Path("namespaces/{namespace}/jobs/{job}/runs")
  public Response create(
      @PathParam("namespace") final String namespace,
      @PathParam("job") final String job,
      @Valid final CreateJobRunRequest request)
      throws ResourceException {
    try {
      if (!namespaceService.exists(namespace)) {
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
              request.getNominalStartTime() == null
                  ? null
                  : Timestamp.valueOf(request.getNominalStartTime()),
              request.getNominalEndTime() == null
                  ? null
                  : Timestamp.valueOf(request.getNominalEndTime()));
      return Response.status(Response.Status.CREATED)
          .entity(coreJobRunToApiJobRunMapper.map(createdJobRun))
          .build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{runId}")
  public Response get(@PathParam("runId") final UUID runId) throws ResourceException {
    try {
      final Optional<JobRun> jobRun = jobService.getJobRun(runId);
      if (jobRun.isPresent()) {
        return Response.ok().entity(coreJobRunToApiJobRunMapper.map(jobRun.get())).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @PUT
  @Timed
  @Path("/jobs/runs/{runId}/complete")
  public Response completeJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      final Optional<JobRun> jobRun = jobService.getJobRun(UUID.fromString(runId));
      if (jobRun.isPresent()) {
        jobService.updateJobRunState(UUID.fromString(runId), JobRunState.State.COMPLETED);
        return Response.ok().build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @PUT
  @Timed
  @Path("/jobs/runs/{runId}/fail")
  public Response failJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      final Optional<JobRun> jobRun = jobService.getJobRun(UUID.fromString(runId));
      if (jobRun.isPresent()) {
        jobService.updateJobRunState(UUID.fromString(runId), JobRunState.State.FAILED);
        return Response.ok().build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @PUT
  @Timed
  @Path("/jobs/runs/{runId}/abort")
  public Response abortJobRun(@PathParam("runId") final String runId) throws ResourceException {
    try {
      final Optional<JobRun> jobRun = jobService.getJobRun(UUID.fromString(runId));
      if (jobRun.isPresent()) {
        jobService.updateJobRunState(UUID.fromString(runId), JobRunState.State.ABORTED);
        return Response.ok().build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (UnexpectedException | Exception e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }
}
