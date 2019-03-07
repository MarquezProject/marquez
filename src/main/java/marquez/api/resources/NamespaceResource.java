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

import com.codahale.metrics.annotation.Timed;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.mappers.CoreNamespaceToApiNamespaceMapper;
import marquez.api.mappers.NamespaceApiMapper;
import marquez.api.mappers.NamespaceResponseMapper;
import marquez.api.models.NamespaceRequest;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Namespace;
import org.hibernate.validator.constraints.NotBlank;

@Slf4j
@Path("/api/v1")
public final class NamespaceResource {
  private final NamespaceApiMapper namespaceApiMapper = new NamespaceApiMapper();
  private final CoreNamespaceToApiNamespaceMapper coreNamespaceToApiNamespaceMapper =
      new CoreNamespaceToApiNamespaceMapper();
  private final NamespaceService namespaceService;

  public NamespaceResource(@NonNull final NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/namespaces/{namespace}")
  public Response create(
      @PathParam("namespace") @NotBlank String namespaceString, @Valid NamespaceRequest request)
      throws MarquezServiceException {
    final Namespace namespace =
        namespaceService.create(namespaceApiMapper.of(namespaceString, request));
    final NamespaceResponse response = NamespaceResponseMapper.map(namespace);
    return Response.ok(response).build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/namespaces/{namespace}")
  public Response get(@PathParam("namespace") String namespaceString)
      throws MarquezServiceException {
    final Optional<NamespaceResponse> namespaceResponse =
        namespaceService.get(namespaceString).map(NamespaceResponseMapper::map);
    if (namespaceResponse.isPresent()) {
      return Response.ok(namespaceResponse.get()).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/namespaces")
  public Response listNamespaces() throws MarquezServiceException {
    final List<Namespace> namespaces = namespaceService.listNamespaces();
    final List<NamespaceResponse> namespaceResponses =
        coreNamespaceToApiNamespaceMapper.map(namespaces);
    return Response.ok(new NamespacesResponse(namespaceResponses)).build();
  }
}
