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
import marquez.api.exceptions.DatasetUrnNotFoundException;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.mappers.DatasetMapper;
import marquez.api.mappers.DatasetResponseMapper;
import marquez.api.models.DatasetRequest;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.models.DatasetUrn;
import marquez.common.models.NamespaceName;
import marquez.service.DatasetService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
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

  @POST
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces/{namespace}/datasets")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response create(
      @PathParam("namespace") NamespaceName namespaceName, @Valid DatasetRequest request)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    final Dataset newDataset = DatasetMapper.map(request);
    final Dataset dataset = datasetService.create(namespaceName, newDataset);
    final DatasetResponse response = DatasetResponseMapper.map(dataset);
    return Response.ok(response).build();
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces/{namespace}/datasets/{urn}")
  @Produces(APPLICATION_JSON)
  public Response get(
      @PathParam("namespace") NamespaceName namespaceName, @PathParam("urn") DatasetUrn urn)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    final Dataset dataset =
        datasetService.get(urn).orElseThrow(() -> new DatasetUrnNotFoundException(urn));
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
      @PathParam("namespace") NamespaceName namespaceName,
      @QueryParam("limit") @DefaultValue("100") Integer limit,
      @QueryParam("offset") @DefaultValue("0") Integer offset)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    final List<Dataset> datasets = datasetService.getAll(namespaceName, limit, offset);
    final DatasetsResponse response = DatasetResponseMapper.toDatasetsResponse(datasets);
    return Response.ok(response).build();
  }

  private void throwIfNotExists(NamespaceName namespaceName) throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      throw new NamespaceNotFoundException(namespaceName);
    }
  }
}
