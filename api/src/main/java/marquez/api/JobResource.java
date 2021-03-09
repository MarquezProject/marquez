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

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.NonNull;
import lombok.Value;
import marquez.api.exceptions.DatasetNotFoundException;
import marquez.api.exceptions.JobNotFoundException;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.exceptions.RunAlreadyExistsException;
import marquez.api.exceptions.RunNotFoundException;
import marquez.common.models.DatasetId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.DatasetDao;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.RunService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;

@Path("/api/v1")
public class JobResource {
  private final NamespaceService namespaceService;
  private final JobService jobService;
  private final RunService runService;
  private final DatasetDao datasetDao;

  public JobResource(
      @NonNull final NamespaceService namespaceService,
      @NonNull final JobService jobService,
      @NonNull final RunService runService,
      @NonNull final DatasetDao datasetDao) {
    this.namespaceService = namespaceService;
    this.jobService = jobService;
    this.runService = runService;
    this.datasetDao = datasetDao;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createOrUpdate(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("job") JobName jobName,
      @Valid JobMeta jobMeta)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    if (jobMeta.getRunId().isPresent()) {
      throwIfJobDoesNotMatchRun(
          jobMeta.getRunId().get(), namespaceName.getValue(), jobName.getValue());
    }
    throwIfDatasetsNotExist(jobMeta.getInputs());
    throwIfDatasetsNotExist(jobMeta.getOutputs());

    final Job job = jobService.createOrUpdate(namespaceName, jobName, jobMeta);
    return Response.ok(job).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs/{job}")
  @Produces(APPLICATION_JSON)
  public Response get(
      @PathParam("namespace") NamespaceName namespaceName, @PathParam("job") JobName jobName)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);

    final Job job =
        jobService.get(namespaceName, jobName).orElseThrow(() -> new JobNotFoundException(jobName));
    return Response.ok(job).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs")
  @Produces(APPLICATION_JSON)
  public Response list(
      @PathParam("namespace") NamespaceName namespaceName,
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);

    final ImmutableList<Job> jobs = jobService.getAll(namespaceName, limit, offset);
    return Response.ok(new Jobs(jobs)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("namespaces/{namespace}/jobs/{job}/runs")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createRun(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("job") JobName jobName,
      @Valid RunMeta runMeta,
      @Context UriInfo uriInfo)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, jobName);
    throwIfExists(namespaceName, jobName, runMeta.getId().orElse(null));

    final Run run = runService.createRun(namespaceName, jobName, runMeta);
    final URI runLocation = locationFor(uriInfo, run);
    return Response.created(runLocation).entity(run).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs/{job}/runs")
  @Produces(APPLICATION_JSON)
  public Response listRuns(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("job") JobName jobName,
      @QueryParam("limit") @DefaultValue("100") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, jobName);

    final ImmutableList<Run> runs = runService.getAllRunsFor(namespaceName, jobName, limit, offset);
    return Response.ok(new Runs(runs)).build();
  }

  @Path("/jobs/runs/{id}")
  public RunResource runResourceRoot(@PathParam("id") RunId runId) {
    throwIfNotExists(runId);
    return new RunResource(runId, runService);
  }

  @Value
  static class Jobs {
    @NonNull
    @JsonProperty("jobs")
    ImmutableList<Job> value;
  }

  @Value
  static class Runs {
    @NonNull
    @JsonProperty("runs")
    ImmutableList<Run> value;
  }

  void throwIfNotExists(@NonNull NamespaceName namespaceName) throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      throw new NamespaceNotFoundException(namespaceName);
    }
  }

  void throwIfNotExists(@NonNull NamespaceName namespaceName, @NonNull JobName jobName)
      throws MarquezServiceException {
    if (!jobService.exists(namespaceName, jobName)) {
      throw new JobNotFoundException(jobName);
    }
  }

  void throwIfExists(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @Nullable RunId runId)
      throws MarquezServiceException {
    if (runId != null) {
      if (runService.runExists(runId)) {
        throw new RunAlreadyExistsException(namespaceName, jobName, runId);
      }
    }
  }

  void throwIfNotExists(@NonNull RunId runId) throws MarquezServiceException {
    if (!runService.runExists(runId)) {
      throw new RunNotFoundException(runId);
    }
  }

  private void throwIfJobDoesNotMatchRun(RunId runId, String namespaceName, String jobName) {
    Optional<Run> runRow = runService.getRun(runId);
    if (runRow.isEmpty()) {
      throw new RunNotFoundException(runId);
    }
    Run run = runRow.get();
    if (!jobName.equals(run.getJobName()) || !namespaceName.equals(run.getNamespaceName())) {
      throw new RunNotFoundException(runId);
    }
  }

  private void throwIfDatasetsNotExist(ImmutableSet<DatasetId> datasets) {
    if (datasets == null) {
      return;
    }
    for (DatasetId datasetId : datasets) {
      if (!datasetDao.exists(datasetId.getNamespace().getValue(), datasetId.getName().getValue())) {
        throw new DatasetNotFoundException(datasetId.getName());
      }
    }
  }

  URI locationFor(@NonNull UriInfo uriInfo, @NonNull Run run) {
    return uriInfo
        .getBaseUriBuilder()
        .path(JobResource.class)
        .path(RunResource.class, "getRun")
        .build(run.getId().getValue());
  }
}
