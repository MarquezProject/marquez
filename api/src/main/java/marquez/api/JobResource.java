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
import java.net.URI;
import java.util.List;
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
import marquez.api.exceptions.JobNotFoundException;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.service.ServiceFactory;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;

@Path("/api/v1")
public class JobResource extends BaseResource {
  public JobResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
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
      @Valid JobMeta jobMeta) {
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
      @PathParam("namespace") NamespaceName namespaceName, @PathParam("job") JobName jobName) {
    throwIfNotExists(namespaceName);

    final Job job =
        jobService
            .findWithRun(namespaceName.getValue(), jobName.getValue())
            .orElseThrow(() -> new JobNotFoundException(jobName));
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
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    throwIfNotExists(namespaceName);

    final List<Job> jobs = jobService.findAllWithRun(namespaceName.getValue(), limit, offset);
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
      @Context UriInfo uriInfo) {
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
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, jobName);

    final List<Run> runs =
        runService.findAll(namespaceName.getValue(), jobName.getValue(), limit, offset);
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
    List<Job> value;
  }

  @Value
  static class Runs {
    @NonNull
    @JsonProperty("runs")
    List<Run> value;
  }
}
