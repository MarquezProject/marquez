/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.api.resources;

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
import marquez.api.mappers.ApiJobToCoreJobMapper;
import marquez.api.mappers.CoreJobRunToApiJobRunResponseMapper;
import marquez.api.mappers.CoreJobToApiJobMapper;
import marquez.api.models.JobRequest;
import marquez.api.models.JobResponse;
import marquez.api.models.JobRunRequest;
import marquez.api.models.JobsResponse;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Job;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;

@Slf4j
@Path("/api/v1")
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
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("job") JobName jobName,
      @Valid final JobRequest request)
      throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    final Job jobToCreate =
        apiJobToCoreJobMapper.map(
            new JobResponse(
                jobName.getValue(),
                null,
                request.getInputDatasetUrns(),
                request.getOutputDatasetUrns(),
                request.getLocation(),
                request.getDescription().orElse(null)));
    jobToCreate.setNamespaceGuid(namespaceService.get(namespaceName).get().getGuid());
    final Job createdJob = jobService.createJob(namespaceName.getValue(), jobToCreate);
    return Response.status(Response.Status.CREATED)
        .entity(coreJobToApiJobMapper.map(createdJob))
        .build();
  }

  @GET
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Produces(APPLICATION_JSON)
  @Timed
  public Response getJob(
      @PathParam("namespace") NamespaceName namespaceName, @PathParam("job") final JobName jobName)
      throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      return Response.status(Response.Status.NOT_FOUND).entity("Namespace not found").build();
    }
    final Optional<Job> returnedJob =
        jobService.getJob(namespaceName.getValue(), jobName.getValue());
    if (returnedJob.isPresent()) {
      return Response.ok().entity(coreJobToApiJobMapper.map(returnedJob.get())).build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  @Path("/namespaces/{namespace}/jobs")
  public Response listJobs(@PathParam("namespace") NamespaceName namespaceName)
      throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    final List<Job> jobList = jobService.getAllJobsInNamespace(namespaceName.getValue());
    final JobsResponse response = new JobsResponse(coreJobToApiJobMapper.map(jobList));
    return Response.ok().entity(response).build();
  }

  @POST
  @Produces(APPLICATION_JSON)
  @Consumes(APPLICATION_JSON)
  @Path("namespaces/{namespace}/jobs/{job}/runs")
  public Response create(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("job") JobName jobName,
      @Valid final JobRunRequest request)
      throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!jobService.getJob(namespaceName.getValue(), jobName.getValue()).isPresent()) {
      log.error("Could not find job: " + jobName.getValue());
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    JobRun createdJobRun =
        jobService.createJobRun(
            namespaceName.getValue(),
            jobName.getValue(),
            request.getRunArgs().orElse(null),
            request.getNominalStartTime().map(Timestamp::valueOf).orElse(null),
            request.getNominalEndTime().map(Timestamp::valueOf).orElse(null));
    return Response.status(Response.Status.CREATED)
        .entity(coreJobRunToApiJobRunMapper.map(createdJobRun))
        .build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/jobs/runs/{id}")
  public Response get(@PathParam("id") final UUID runId) throws MarquezServiceException {
    final Optional<JobRun> jobRun = jobService.getJobRun(runId);
    if (jobRun.isPresent()) {
      return Response.ok().entity(coreJobRunToApiJobRunMapper.map(jobRun.get())).build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @PUT
  @Timed
  @Path("/jobs/runs/{id}/run")
  public Response runJobRun(@PathParam("id") final String runId) throws MarquezServiceException {
    return processJobRunStateUpdate(runId, JobRunState.State.RUNNING);
  }

  @PUT
  @Timed
  @Path("/jobs/runs/{id}/complete")
  public Response completeJobRun(@PathParam("id") final String runId)
      throws MarquezServiceException {
    return processJobRunStateUpdate(runId, JobRunState.State.COMPLETED);
  }

  @PUT
  @Timed
  @Path("/jobs/runs/{id}/fail")
  public Response failJobRun(@PathParam("id") final String runId) throws MarquezServiceException {
    return processJobRunStateUpdate(runId, JobRunState.State.FAILED);
  }

  @PUT
  @Timed
  @Path("/jobs/runs/{id}/abort")
  public Response abortJobRun(@PathParam("id") final String runId) throws MarquezServiceException {
    return processJobRunStateUpdate(runId, JobRunState.State.ABORTED);
  }

  private Response processJobRunStateUpdate(String runId, JobRunState.State state)
      throws MarquezServiceException {
    final UUID jobRunUuid;
    try {
      jobRunUuid = UUID.fromString(runId);
    } catch (IllegalArgumentException e) {
      final String errorMsg = "Could not parse " + runId + " into a UUID!";
      log.error(errorMsg, e);
      return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
    }
    final Optional<JobRun> jobRun = jobService.getJobRun(jobRunUuid);
    if (jobRun.isPresent()) {
      jobService.updateJobRunState(jobRunUuid, state);
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
