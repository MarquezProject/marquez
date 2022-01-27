/* SPDX-License-Identifier: Apache-2.0 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.concurrent.ExecutionException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.service.ServiceFactory;
import marquez.service.models.NodeId;

/**
 * @deprecated This resource is no longer supported
 *     <p>Use {@link OpenLineageResource#getLineage(NodeId, int)} instead.
 */
@Path("/api/v1-beta")
@Slf4j
@Deprecated(since = "0.16.0")
public class LineageResource extends BaseResource {
  private static final String DEFAULT_DEPTH = "20";

  public LineageResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/lineage")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response getLineage(
      @QueryParam("nodeId") @NotNull NodeId nodeId,
      @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth)
      throws ExecutionException, InterruptedException {
    return Response.ok(lineageService.lineage(nodeId, depth)).build();
  }
}
