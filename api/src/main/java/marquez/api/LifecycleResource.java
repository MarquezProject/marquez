package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.Value;
import marquez.db.LifecycleDao;
import marquez.service.LifecycleService.Lifecycle;

@Path("/api/v1")
public class LifecycleResource {
  private final LifecycleDao lifecycleDao;

  public LifecycleResource(@NonNull final LifecycleDao lifecycleDao) {
    this.lifecycleDao = lifecycleDao;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/lifecycle_events")
  @Produces(APPLICATION_JSON)
  public Response listLifecycleEvents(
      @QueryParam("namespace") String namespace,
      @QueryParam("limit") @DefaultValue("10") @Min(value = 0) int limit) {
    final List<Lifecycle.Event> lifecycleEvents =
        lifecycleDao.findAllLifecycleEvents(namespace, limit);
    return Response.ok(new LifecycleEvents(lifecycleEvents)).build();
  }

  @Value
  static class LifecycleEvents {
    @NonNull
    @JsonProperty("lifecycle_events")
    List<Lifecycle.Event> value;
  }
}
