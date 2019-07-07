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

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.mappers.JobMapper;
import marquez.api.mappers.JobResponseMapper;
import marquez.api.mappers.JobRunResponseMapper;
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

  public JobResource(final NamespaceService namespaceService, final JobService jobService) {
    this.namespaceService = namespaceService;
    this.jobService = jobService;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response create(
      @PathParam("namespace") String namespaceAsString,
      @PathParam("job") String jobAsString,
      @Valid final JobRequest request)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceAsString);
    throwIfNotExists(namespaceName);
    final Job newJob = JobMapper.map(JobName.of(jobAsString), request);
    newJob.setNamespaceGuid(namespaceService.get(namespaceName).get().getGuid());
    final Job job = jobService.createJob(namespaceName.getValue(), newJob);
    final JobResponse response = JobResponseMapper.map(job);
    return Response.status(Response.Status.CREATED).entity(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Produces(APPLICATION_JSON)
  public Response getJob(
      @PathParam("namespace") String namespaceAsString, @PathParam("job") String jobAsString)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceAsString);
    throwIfNotExists(namespaceName);
    final Optional<Job> returnedJob = jobService.getJob(namespaceName.getValue(), jobAsString);
    if (returnedJob.isPresent()) {
      return Response.ok().entity(JobResponseMapper.map(returnedJob.get())).build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs")
  @Produces(APPLICATION_JSON)
  public Response listJobs(
      @PathParam("namespace") String namespaceAsString,
      @QueryParam("limit") @DefaultValue("100") Integer limit,
      @QueryParam("offset") @DefaultValue("0") Integer offset)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceAsString);
    throwIfNotExists(namespaceName);
    final List<Job> jobs =
        jobService.getAllJobsInNamespace(namespaceName.getValue(), limit, offset);
    final JobsResponse response = JobResponseMapper.toJobsResponse(jobs);
    return Response.ok().entity(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("namespaces/{namespace}/jobs/{job}/runs")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response create(
      @PathParam("namespace") String namespaceAsString,
      @PathParam("job") String jobAsString,
      @Valid final JobRunRequest request)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceAsString);
    throwIfNotExists(namespaceName);
    if (!jobService.getJob(namespaceName.getValue(), jobAsString).isPresent()) {
      log.error("Could not find job: " + jobAsString);
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    JobRun createdJobRun =
        jobService.createJobRun(
            namespaceName.getValue(),
            jobAsString,
            request.getRunArgs().orElse(null),
            request.getNominalStartTime().map(Instant::parse).orElse(null),
            request.getNominalEndTime().map(Instant::parse).orElse(null));
    return Response.status(Response.Status.CREATED)
        .entity(JobRunResponseMapper.map(createdJobRun))
        .build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs/{job}/runs")
  @Produces(APPLICATION_JSON)
  public Response getRunsForJob(
      @PathParam("namespace") String namespaceAsString,
      @PathParam("job") String jobAsString,
      @QueryParam("limit") @DefaultValue("100") Integer limit,
      @QueryParam("offset") @DefaultValue("0") Integer offset)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceAsString);
    throwIfNotExists(namespaceName);
    final List<JobRun> jobRuns =
        jobService.getAllRunsOfJob(namespaceName, jobAsString, limit, offset);
    return Response.ok().entity(JobRunResponseMapper.map(jobRuns)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/jobs/runs/{id}")
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("id") final UUID runId) throws MarquezServiceException {
    final Optional<JobRun> jobRun = jobService.getJobRun(runId);
    if (jobRun.isPresent()) {
      return Response.ok().entity(JobRunResponseMapper.map(jobRun.get())).build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("/jobs/runs/{id}/run")
  public Response runJobRun(@PathParam("id") final String runId) throws MarquezServiceException {
    return processJobRunStateUpdate(runId, JobRunState.State.RUNNING);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("/jobs/runs/{id}/complete")
  public Response completeJobRun(@PathParam("id") final String runId)
      throws MarquezServiceException {
    return processJobRunStateUpdate(runId, JobRunState.State.COMPLETED);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("/jobs/runs/{id}/fail")
  public Response failJobRun(@PathParam("id") final String runId) throws MarquezServiceException {
    return processJobRunStateUpdate(runId, JobRunState.State.FAILED);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
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

  private void throwIfNotExists(NamespaceName namespaceName) throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      throw new NamespaceNotFoundException(namespaceName);
    }
  }
}
