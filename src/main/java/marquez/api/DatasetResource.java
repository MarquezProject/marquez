package marquez.api;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import marquez.api.mappers.DatasetResponseMapper;
import marquez.api.models.DatasetResponse;
import marquez.api.models.ListDatasetsResponse;
import marquez.service.DatasetService;
import marquez.service.models.Dataset;

@Path("/api/v1")
public final class DatasetResource {
  private final DatasetResponseMapper datasetResponseMapper = new DatasetResponseMapper();
  private final DatasetService datasetService;

  public DatasetResource(final DatasetService datasetService) {
    this.datasetService = requireNonNull(datasetService);
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces/{namespace}/datasets")
  @Produces(APPLICATION_JSON)
  public Response list(
      @PathParam("namespace") String namespace,
      @QueryParam("limit") Integer limit,
      @QueryParam("offset") Integer offset) {
    final List<Dataset> datasets = datasetService.getAll(namespace, limit, offset);
    final List<DatasetResponse> datasetResponse = datasetResponseMapper.map(datasets);
    return Response.ok(new ListDatasetsResponse(datasetResponse)).build();
  }
}
