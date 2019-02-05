package marquez.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static marquez.api.mappers.DatasetResponseMapper.map;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.models.Namespace;
import marquez.service.DatasetService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.Dataset;

@Path("/api/v1")
public final class DatasetResource {
  private final NamespaceService namespaceService;
  private final DatasetService datasetService;

  public DatasetResource(
      @NonNull final NamespaceService namespaceService,
      @NonNull final DatasetService datasetService) {
    this.namespaceService = namespaceService;
    this.datasetService = datasetService;
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces/{namespace}/datasets")
  @Produces(APPLICATION_JSON)
  public Response list(
      @PathParam("namespace") String namespaceString,
      @QueryParam("limit") @DefaultValue("100") Integer limit,
      @QueryParam("offset") @DefaultValue("0") Integer offset)
      throws UnexpectedException, WebApplicationException {
    if (!namespaceService.exists(namespaceString)) {
      throw new WebApplicationException(
          String.format("The namespace %s does not exist.", namespaceString), NOT_FOUND);
    }
    final List<Dataset> datasets =
        datasetService.getAll(Namespace.of(namespaceString), limit, offset);
    final List<DatasetResponse> datasetResponses = map(datasets);
    return Response.ok(new DatasetsResponse(datasetResponses)).build();
  }
}
