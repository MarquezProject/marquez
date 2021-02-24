package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static marquez.common.models.RunState.ABORTED;
import static marquez.common.models.RunState.COMPLETED;
import static marquez.common.models.RunState.FAILED;
import static marquez.common.models.RunState.RUNNING;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import marquez.api.exceptions.RunNotFoundException;
import marquez.common.Utils;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.service.RunService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Run;

public class RunResource {

  private final RunId runId;
  private final RunService runService;

  public RunResource(RunId runId, RunService runService) {
    this.runId = runId;
    this.runService = runService;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/")
  @Produces(APPLICATION_JSON)
  public Response getRun() throws MarquezServiceException {
    final Run run = runService.getRun(runId).orElseThrow(() -> new RunNotFoundException(runId));
    return Response.ok(run).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("start")
  @Produces(APPLICATION_JSON)
  public Response markRunAsRunning(@QueryParam("at") String atAsIso)
      throws MarquezServiceException {
    return markRunAs(RUNNING, atAsIso);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("complete")
  @Produces(APPLICATION_JSON)
  public Response markRunAsCompleted(@QueryParam("at") String atAsIso)
      throws MarquezServiceException {
    return markRunAs(COMPLETED, atAsIso);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("fail")
  @Produces(APPLICATION_JSON)
  public Response markRunAsFailed(@QueryParam("at") String atAsIso) throws MarquezServiceException {
    return markRunAs(FAILED, atAsIso);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("abort")
  @Produces(APPLICATION_JSON)
  public Response markRunAsAborted(@QueryParam("at") String atAsIso)
      throws MarquezServiceException {
    return markRunAs(ABORTED, atAsIso);
  }

  Response markRunAs(@NonNull RunState runState, @QueryParam("at") String atAsIso) {
    runService.markRunAs(runId, runState, Utils.toInstant(atAsIso));
    return getRun();
  }
}
