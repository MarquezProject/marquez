package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import marquez.api.Job;
import marquez.api.JobRunDefinition;
import marquez.api.JobVersion;
import marquez.api.Owner;
import marquez.api.entities.*;
import marquez.db.dao.JobDAO;
import marquez.db.dao.JobRunDefinitionDAO;
import marquez.db.dao.JobVersionDAO;
import marquez.db.dao.OwnerDAO;

@Path("/job_run_definition")
public final class JobRunDefinitionResource extends BaseResource {
  private JobVersionDAO jobVersionDAO;
  private JobRunDefinitionDAO jobRunDefDAO;
  private JobDAO jobDAO;
  private OwnerDAO ownerDAO;

  public JobRunDefinitionResource(
      final JobRunDefinitionDAO jobRunDefDAO,
      final JobVersionDAO jobVersionDAO,
      final JobDAO jobDAO,
      final OwnerDAO ownerDAO) {
    this.jobRunDefDAO = jobRunDefDAO;
    this.jobVersionDAO = jobVersionDAO;
    this.jobDAO = jobDAO;
    this.ownerDAO = ownerDAO;
  }

  private Owner findOrCreateOwner(CreateJobRunDefinitionRequest request) {
    Owner owner = this.ownerDAO.findByName(request.getOwnerName());
    if (owner == null) {
      owner = new Owner(request.getOwnerName());
      this.ownerDAO.insert(UUID.randomUUID(), owner);
    }
    return owner;
  }

  private Job findOrCreateJob(CreateJobRunDefinitionRequest request) {
    Job matchingJob = this.jobDAO.findByName(request.getName());
    if (matchingJob != null) {
      return matchingJob;
    }
    UUID jobGuid = UUID.randomUUID();
    Job newJob = new Job(jobGuid, request.getName(), request.getOwnerName(), null, null, null);
    this.jobDAO.insert(newJob);
    return newJob;
  }

  private UUID findOrCreateJobVersion(CreateJobRunDefinitionRequest request, Job job) {
    JobVersion reqJV = JobVersion.create(request);
    UUID computedVersion = reqJV.computeVersionGuid();
    JobVersion matchingJobVersion = this.jobVersionDAO.findByVersion(computedVersion);
    if (matchingJobVersion != null) {
      return matchingJobVersion.getGuid();
    }
    UUID jobVersionGuid = UUID.randomUUID();
    this.jobVersionDAO.insert(jobVersionGuid, computedVersion, job.getGuid(), request.getURI());
    return jobVersionGuid;
  }

  private JobRunDefinition findOrCreateJobRunDefinition(
      CreateJobRunDefinitionRequest request, UUID jobVersionGuid) {
    JobRunDefinition reqJrd = JobRunDefinition.create(request, jobVersionGuid);
    UUID definitionHash = reqJrd.computeDefinitionHash();
    JobRunDefinition existingJrd = jobRunDefDAO.findByHash(definitionHash);

    if (existingJrd != null) {
      return existingJrd;
    }

    UUID jobRunDefGuid = UUID.randomUUID();
    this.jobRunDefDAO.insert(
        jobRunDefGuid,
        definitionHash,
        jobVersionGuid,
        request.getRunArgsJson(),
        request.getNominalStartTime(),
        request.getNominalEndTime());
    return jobRunDefDAO.findByGuid(jobRunDefGuid);
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @Timed
  public Response create(@Valid CreateJobRunDefinitionRequest request) {
    if (!request.validate()) {
      return Response.status(Status.BAD_REQUEST)
          .entity(new ErrorResponse("run_args and/or uri not well-formed."))
          .type(APPLICATION_JSON)
          .build();
    }

    try {
      Owner owner = findOrCreateOwner(request);
      Job job = findOrCreateJob(request);
      UUID jobVersionGuid = findOrCreateJobVersion(request, job);
      JobRunDefinition jobRunDefinition = findOrCreateJobRunDefinition(request, jobVersionGuid);
      CreateJobRunDefinitionResponse res =
          new CreateJobRunDefinitionResponse(jobRunDefinition.getGuid());
      return Response.status(Response.Status.OK)
          .header("Location", "/job_run_definition/" + jobRunDefinition.getGuid())
          .entity(res)
          .type(APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  @Path("/{id}")
  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  public Response read(@PathParam("id") String id) {
    UUID jobRunDefinitionId;
    try {
      jobRunDefinitionId = UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("id is not well-formed."))
          .type(APPLICATION_JSON)
          .build();
    }
    JobRunDefinition jrd = jobRunDefDAO.findByGuid(jobRunDefinitionId);
    if (jrd == null) {
      return Response.status(Response.Status.NOT_FOUND.getStatusCode())
          .entity(new ErrorResponse("no Job Run Definition found."))
          .type(APPLICATION_JSON)
          .build();
    }
    GetJobRunDefinitionResponse jrdRes = GetJobRunDefinitionResponse.create(jrd);
    return Response.ok(jrdRes, APPLICATION_JSON).build();
  }
}
