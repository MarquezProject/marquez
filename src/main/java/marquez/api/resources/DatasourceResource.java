/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.exceptions.DatasourceUrnNotFoundException;
import marquez.api.mappers.DatasourceResponseMapper;
import marquez.api.models.DatasourceRequest;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.service.DatasourceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Datasource;

@Slf4j
@Path("/api/v1/datasources")
public final class DatasourceResource {

  private final DatasourceService datasourceService;

  public DatasourceResource(@NonNull final DatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  @POST
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response create(@Valid @NonNull final DatasourceRequest request)
      throws MarquezServiceException {

    final ConnectionUrl connectionUrl;
    try {
      connectionUrl = ConnectionUrl.fromString(request.getConnectionUrl());
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    final Datasource createdDatasource =
        datasourceService.create(connectionUrl, DatasourceName.fromString(request.getName()));
    return Response.status(CREATED).entity(DatasourceResponseMapper.map(createdDatasource)).build();
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Produces(APPLICATION_JSON)
  @Path("/{urn}")
  public Response get(@PathParam("urn") @NonNull final DatasourceUrn urn)
      throws MarquezServiceException {

    final Datasource datasource =
        datasourceService.get(urn).orElseThrow(() -> new DatasourceUrnNotFoundException(urn));
    return Response.ok(DatasourceResponseMapper.map(datasource)).build();
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Produces(APPLICATION_JSON)
  public Response list(
      @QueryParam("limit") @DefaultValue("100") Integer limit,
      @QueryParam("offset") @DefaultValue("0") Integer offset) {

    final List<Datasource> datasources = datasourceService.getAll(limit, offset);
    return Response.ok(DatasourceResponseMapper.toDatasourcesResponse(datasources)).build();
  }
}
