package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.net.URI;
import java.sql.Timestamp;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import marquez.api.CreateJobRunRequest;
import marquez.api.CreateJobRunResponse;
import marquez.api.JobRun;
import marquez.db.dao.JobRunDAO;

@Path("/job_runs")
@Produces(APPLICATION_JSON)
public class JobRunResource extends BaseResource {

  private final JobRunDAO dao;

  public JobRunResource(final JobRunDAO dao) {
    this.dao = dao;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(@Valid CreateJobRunRequest request) {
    UUID jobRunGuid = UUID.randomUUID();

    try {
      JobRun jobRun =
          new JobRun(
              jobRunGuid,
              new Timestamp(System.currentTimeMillis()),
              request.getStartedAt(),
              null,
              request.getJobRunDefinitionGuid(),
              request.getCurrentState());
      dao.insert(jobRun);

      CreateJobRunResponse res = new CreateJobRunResponse(jobRunGuid);
      return Response.created(URI.create("/job_runs/" + res.getExternalGuid())).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }
}
