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
import marquez.api.exceptions.DatasourceUrnNotFoundException;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.mappers.DatasetMapper;
import marquez.api.mappers.DatasetResponseMapper;
import marquez.api.models.DatasetRequest;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.models.NamespaceName;
import marquez.service.DatasetService;
import marquez.service.DatasourceService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Dataset;
import marquez.service.models.Datasource;

@Path("/api/v1")
public final class DatasetResource {
  private final NamespaceService namespaceService;
  private final DatasourceService datasourceService;
  private final DatasetService datasetService;

  public DatasetResource(
      @NonNull final NamespaceService namespaceService,
      @NonNull final DatasourceService datasourceService,
      @NonNull final DatasetService datasetService) {
    this.namespaceService = namespaceService;
    this.datasourceService = datasourceService;
    this.datasetService = datasetService;
  }

  @POST
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces/{namespace}/datasets")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response create(
      @NonNull @PathParam("namespace") NamespaceName namespaceName,
      @NonNull @Valid DatasetRequest request)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    final Datasource datasource =
        datasourceService
            .get(request.getDatasourceUrn())
            .orElseThrow(() -> new DatasourceUrnNotFoundException(request.getDatasourceUrn()));
    final Dataset newDataset = DatasetMapper.map(request);
    final Dataset dataset =
        datasetService.create(namespaceName, datasource.getName(), datasource.getUrn(), newDataset);
    final DatasetResponse response = DatasetResponseMapper.map(dataset);
    return Response.ok(response).build();
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces/{namespace}/datasets")
  @Produces(APPLICATION_JSON)
  public Response list(
      @NonNull @PathParam("namespace") NamespaceName namespaceName,
      @NonNull @QueryParam("limit") @DefaultValue("100") Integer limit,
      @NonNull @QueryParam("offset") @DefaultValue("0") Integer offset)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    final List<Dataset> datasets = datasetService.getAll(namespaceName, limit, offset);
    final DatasetsResponse response = DatasetResponseMapper.toDatasetsResponse(datasets);
    return Response.ok(response).build();
  }

  private void throwIfNotExists(@NonNull NamespaceName namespaceName)
      throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      throw new NamespaceNotFoundException(namespaceName);
    }
  }
}
