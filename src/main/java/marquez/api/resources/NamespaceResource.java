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
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.mappers.NamespaceApiMapper;
import marquez.api.mappers.NamespaceResponseMapper;
import marquez.api.models.NamespaceRequest;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.common.models.NamespaceName;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Namespace;

@Path("/api/v1")
public final class NamespaceResource {
  private final NamespaceApiMapper namespaceApiMapper = new NamespaceApiMapper();
  private final NamespaceService namespaceService;

  public NamespaceResource(@NonNull final NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  @PUT
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces/{namespace}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createOrUpdate(
      @PathParam("namespace") NamespaceName namespaceName, @Valid NamespaceRequest request)
      throws MarquezServiceException {
    final Namespace namespace =
        namespaceService.createOrUpdate(namespaceApiMapper.of(namespaceName.getValue(), request));
    final NamespaceResponse response = NamespaceResponseMapper.map(namespace);
    return Response.ok(response).build();
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces/{namespace}")
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("namespace") NamespaceName namespaceName)
      throws MarquezServiceException {
    final Namespace namespace =
        namespaceService
            .get(namespaceName)
            .orElseThrow(() -> new NamespaceNotFoundException(namespaceName));
    final NamespaceResponse response = NamespaceResponseMapper.map(namespace);
    return Response.ok(response).build();
  }

  @GET
  @ResponseMetered
  @ExceptionMetered
  @Timed
  @Path("/namespaces")
  @Produces(APPLICATION_JSON)
  public Response list() throws MarquezServiceException {
    final List<Namespace> namespaces = namespaceService.getAll();
    final NamespacesResponse response = NamespaceResponseMapper.toNamespacesResponse(namespaces);
    return Response.ok(response).build();
  }
}
