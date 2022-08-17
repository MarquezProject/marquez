/* SPDX-License-Identifier: Apache-2.0 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import marquez.api.exceptions.RunNotFoundException;
import marquez.common.models.RunId;
import marquez.db.RunDao;
import marquez.service.models.Run;

@Path("/api/v1")
public class RunResource {
  private final RunDao runDao;

  public RunResource(@NonNull final RunDao runDao) {
    this.runDao = runDao;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/jobs/runs/{id}")
  @Produces(APPLICATION_JSON)
  public Response getRun(@PathParam("id") RunId runId) {
    throwIfNotExists(runId);
    final Run run =
        runDao.findRunByUuid(runId.getValue()).orElseThrow(() -> new RunNotFoundException(runId));
    return Response.ok(run).build();
  }

  /** Throws {@link RunNotFoundException} if the provided {@link RunId} is not found. */
  void throwIfNotExists(@NonNull RunId runId) {
    if (!runDao.exists(runId.getValue())) {
      throw new RunNotFoundException(runId);
    }
  }
}
