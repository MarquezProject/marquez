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
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static marquez.api.mappers.DatasetResponseMapper.map;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
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
      throws MarquezServiceException, WebApplicationException {
    if (!namespaceService.exists(namespaceName)) {
      throw new WebApplicationException(
          String.format("The namespace %s does not exist.", namespaceName.getValue()), NOT_FOUND);
    }
    try {
      String namespaceNameString = namespaceName.getValue();
      final List<Dataset> datasets =
          datasetService.getAll(NamespaceName.fromString(namespaceNameString), limit, offset);
      final List<DatasetResponse> datasetResponses = map(datasets);
      return Response.ok(new DatasetsResponse(datasetResponses)).build();
    } catch (Exception e) {
      throw new InternalServerErrorException("unexpected error while fetching datasets", e);
    }
  }
}
