package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import marquez.api.entities.*;
import marquez.api.JobVersion;
import marquez.api.JobRunDefinition;
import marquez.db.dao.JobRunDefinitionDAO;
import marquez.db.dao.JobVersionDAO;

@Path("/job_run_definition")
public final class JobRunDefinitionResource extends BaseResource {
  private JobVersionDAO jobVersionDAO;
  private JobRunDefinitionDAO jobRunDefDAO;

  public JobRunDefinitionResource(final JobRunDefinitionDAO jobRunDefDAO, final JobVersionDAO jobVersionDAO) {
    this.jobRunDefDAO = jobRunDefDAO;
    this.jobVersionDAO = jobVersionDAO;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(@Valid CreateJobRunDefinitionRequest request) {
    // INPROGRESS: replace this with actual job detection / insertion
    UUID jobGuid = UUID.fromString("a35bedfe-f913-4167-9562-80c5e22bfbc4");

    // determine if version is new or not
    JobRunDefinition reqJrd = JobRunDefinition.create(request);
    UUID computedVersion = reqJrd.computeVersionGuid();
    JobVersion matchingJobVersion = this.jobVersionDAO.findByVersion(computedVersion);

    UUID jobVersionGuid;
    if(matchingJobVersion == null) {
      // insert new job version
      jobVersionGuid = UUID.randomUUID();
      this.jobVersionDAO.insert(jobVersionGuid, computedVersion, jobGuid, request.getURI());
    } else {
      jobVersionGuid = matchingJobVersion.getGuid();
    }

    // generate new uuid for Job Run Definition
    UUID jobRunDefGuid = UUID.randomUUID();

    // insert rows as needed
    this.jobRunDefDAO.insert(jobRunDefGuid, jobVersionGuid, request.getRunArgsJson(), request.getURI());

    // return Job Run Definition Guid
    CreateJobRunDefinitionResponse res = new CreateJobRunDefinitionResponse(jobRunDefGuid);
    try {
      String jsonRes = mapper.writeValueAsString(res);
      return Response.ok(jsonRes, APPLICATION_JSON).build();
    } catch (JsonProcessingException e) {
      return Response.serverError().build();
    }
  }
}
