/*
 *
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

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.List;
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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.exceptions.JobNotFoundException;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.exceptions.RunNotFoundException;
import marquez.api.exceptions.RunNotValidException;
import marquez.api.mappers.Mapper;
import marquez.api.models.JobRequest;
import marquez.api.models.JobResponse;
import marquez.api.models.JobsResponse;
import marquez.api.models.RunRequest;
import marquez.api.models.RunResponse;
import marquez.api.models.RunsResponse;
import marquez.common.Utils;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;

@Slf4j
@Path("/api/v1")
public final class JobResource {
  private final NamespaceService namespaceService;
  private final JobService jobService;

  public JobResource(
      @NonNull final NamespaceService namespaceService, @NonNull final JobService jobService) {
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
  public Response createOrUpdate(
      @PathParam("namespace") String namespaceString,
      @PathParam("job") String jobString,
      @Valid JobRequest request)
      throws MarquezServiceException {
    log.debug("Request: {}", request);
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);

    final JobName jobName = JobName.of(jobString);
    final JobMeta jobMeta = Mapper.toJobMeta(request);
    final Job job = jobService.createOrUpdate(namespaceName, jobName, jobMeta);
    final JobResponse response = Mapper.toJobResponse(job);
    log.debug("Response: {}", response);
    return Response.ok(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Produces(APPLICATION_JSON)
  public Response get(
      @PathParam("namespace") String namespaceString, @PathParam("job") String jobString)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);

    final JobName jobName = JobName.of(jobString);
    final JobResponse response =
        jobService
            .get(namespaceName, jobName)
            .map(Mapper::toJobResponse)
            .orElseThrow(() -> new JobNotFoundException(jobName));
    log.debug("Response: {}", response);
    return Response.ok(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs")
  @Produces(APPLICATION_JSON)
  public Response list(
      @PathParam("namespace") String namespaceString,
      @QueryParam("limit") @DefaultValue("100") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);

    final List<Job> jobs = jobService.getAll(namespaceName, limit, offset);
    final JobsResponse response = Mapper.toJobsResponse(jobs);
    log.debug("Response: {}", response);
    return Response.ok(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("namespaces/{namespace}/jobs/{job}/runs")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createRun(
      @PathParam("namespace") String namespaceString,
      @PathParam("job") String jobString,
      @Valid RunRequest request)
      throws MarquezServiceException {
    log.debug("Request: {}", request);
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);
    final JobName jobName = JobName.of(jobString);
    throwIfNotExists(namespaceName, jobName);

    final RunMeta runMeta = Mapper.toRunMeta(request);
    final Run run = jobService.createRun(namespaceName, jobName, runMeta);
    final RunResponse response = Mapper.toRunResponse(run);
    log.debug("Response: {}", response);
    return Response.status(CREATED).entity(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs/{job}/runs")
  @Produces(APPLICATION_JSON)
  public Response listRuns(
      @PathParam("namespace") String namespaceString,
      @PathParam("job") String jobString,
      @QueryParam("limit") @DefaultValue("100") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);
    final JobName jobName = JobName.of(jobString);
    throwIfNotExists(namespaceName, jobName);

    final List<Run> runs = jobService.getAllRunsFor(namespaceName, jobName, limit, offset);
    final RunsResponse response = Mapper.toRunsResponse(runs);
    log.debug("Response: {}", response);
    return Response.ok(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/jobs/runs/{id}")
  @Produces(APPLICATION_JSON)
  public Response getRun(@PathParam("id") String runIdString) throws MarquezServiceException {
    final UUID runId = toRunIdOrThrow(runIdString);
    final RunResponse response =
        jobService
            .getRun(runId)
            .map(Mapper::toRunResponse)
            .orElseThrow(() -> new RunNotFoundException(runId));
    log.debug("Response: {}", response);
    return Response.ok(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("/jobs/runs/{id}/start")
  @Produces(APPLICATION_JSON)
  public Response markRunAsRunning(@PathParam("id") String runIdString)
      throws MarquezServiceException {
    return markRunAs(runIdString, Run.State.RUNNING);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("/jobs/runs/{id}/complete")
  @Produces(APPLICATION_JSON)
  public Response markRunAsCompleted(@PathParam("id") String runIdString)
      throws MarquezServiceException {
    return markRunAs(runIdString, Run.State.COMPLETED);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("/jobs/runs/{id}/fail")
  @Produces(APPLICATION_JSON)
  public Response markRunAsFailed(@PathParam("id") String runIdString)
      throws MarquezServiceException {
    return markRunAs(runIdString, Run.State.FAILED);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("/jobs/runs/{id}/abort")
  @Produces(APPLICATION_JSON)
  public Response markRunAsAborted(@PathParam("id") String runIdString)
      throws MarquezServiceException {
    return markRunAs(runIdString, Run.State.ABORTED);
  }

  private UUID toRunIdOrThrow(@NonNull String runIdString) {
    try {
      return Utils.toUuid(runIdString);
    } catch (IllegalArgumentException e) {
      throw new RunNotValidException(runIdString);
    }
  }

  private Response markRunAs(String runIdString, Run.State runState)
      throws MarquezServiceException {
    final UUID runId = toRunIdOrThrow(runIdString);
    throwIfNotExists(runId);

    jobService.markRunAs(runId, runState);
    return getRun(runIdString);
  }

  private void throwIfNotExists(@NonNull NamespaceName namespaceName)
      throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      throw new NamespaceNotFoundException(namespaceName);
    }
  }

  private void throwIfNotExists(@NonNull NamespaceName namespaceName, @NonNull JobName jobName)
      throws MarquezServiceException {
    if (!jobService.exists(namespaceName, jobName)) {
      throw new JobNotFoundException(jobName);
    }
  }

  private void throwIfNotExists(@NonNull UUID runId) throws MarquezServiceException {
    if (!jobService.runExists(runId)) {
      throw new RunNotFoundException(runId);
    }
  }
}
