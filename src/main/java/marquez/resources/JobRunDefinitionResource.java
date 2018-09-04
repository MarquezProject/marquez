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
import marquez.api.CreateJobRunDefinitionRequest;
import marquez.api.CreateJobRunDefinitionResponse;
import marquez.db.dao.JobRunDefinitionDAO;

@Path("/job_run_definition")
public final class JobRunDefinitionResource extends BaseResource {
  private JobRunDefinitionDAO dao;

  public JobRunDefinitionResource(final JobRunDefinitionDAO dao) {
    this.dao = dao;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(@Valid CreateJobRunDefinitionRequest request) {
    // determine if version is new or not

    // generate new uuid for Job Run Definition
    UUID jobRunDefGuid = UUID.randomUUID();

    // insert rows as needed
    this.dao.insert(jobRunDefGuid, null, request.getRunArgsJson(), request.getURI());

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
