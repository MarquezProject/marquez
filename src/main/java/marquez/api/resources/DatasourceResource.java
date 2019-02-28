package marquez.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import marquez.api.mappers.DatasourceResponseMapper;
import marquez.service.DatasourceService;

@Path("/api/v1")
public final class DatasourceResource {

  private final DatasourceService datasourceService;

  public DatasourceResource(DatasourceService dataSourceService) {
    this.datasourceService = dataSourceService;
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/datasources")
  @Produces(APPLICATION_JSON)
  public Response list(
      @QueryParam("limit") @DefaultValue("100") Integer limit,
      @QueryParam("offset") @DefaultValue("0") Integer offset) {

    List<marquez.service.models.Datasource> datasourceList =
        datasourceService.getAll(limit, offset);
    return Response.ok(DatasourceResponseMapper.toDatasourcesResponse(datasourceList)).build();
  }
}
