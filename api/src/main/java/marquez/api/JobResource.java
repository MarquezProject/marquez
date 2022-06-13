/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import marquez.api.exceptions.JobNotFoundException;
import marquez.api.exceptions.JobVersionNotFoundException;
import marquez.api.models.JobVersion;
import marquez.api.models.ResultsPage;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.Version;
import marquez.db.JobVersionDao;
import marquez.db.models.JobRow;
import marquez.service.ServiceFactory;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;

@Path("/api/v1")
public class JobResource extends BaseResource {
  private final JobVersionDao jobVersionDao;

  public JobResource(
      @NonNull final ServiceFactory serviceFactory, @NonNull final JobVersionDao jobVersionDao) {
    super(serviceFactory);
    this.jobVersionDao = jobVersionDao;
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
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
  public Response getJob(
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
  @Path("/namespaces/{namespace}/jobs/{job}/versions/{version}")
  @Produces(APPLICATION_JSON)
  public Response getJobVersion(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("job") JobName jobName,
      @PathParam("version") Version version) {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, jobName);

    final JobVersion jobVersion =
        jobVersionDao
            .findJobVersion(namespaceName.getValue(), jobName.getValue(), version.getValue())
            .orElseThrow(() -> new JobVersionNotFoundException(version));
    return Response.ok(jobVersion).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}/jobs/{job}/versions")
  @Produces(APPLICATION_JSON)
  public Response listJobVersions(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("job") JobName jobName,
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, jobName);

    final List<JobVersion> jobVersions =
        jobVersionDao.findAllJobVersions(
            namespaceName.getValue(), jobName.getValue(), limit, offset);
    return Response.ok(new JobVersions(jobVersions)).build();
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
    final int totalCount = jobService.countFor(namespaceName.getValue());
    return Response.ok(new ResultsPage<>("jobs", jobs, totalCount)).build();
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
    JobRow job =
        jobService
            .findJobByNameAsRow(namespaceName.getValue(), jobName.getValue())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format(
                            "No such job with namespace %s and name %s",
                            namespaceName.getValue(), jobName.getValue())));

    final Run run = runService.createRun(namespaceName, job, runMeta);
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
  static class JobVersions {
    @NonNull
    @JsonProperty("versions")
    List<JobVersion> value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  public static class Runs {
    @NonNull
    @JsonProperty("runs")
    List<Run> value;
  }
}
