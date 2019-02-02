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
import marquez.api.mappers.NamespaceResponseMapper;
import marquez.api.models.CreateNamespaceRequest;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;
import marquez.core.mappers.NamespaceApiMapper;
import marquez.core.models.Namespace;
import marquez.service.NamespaceService;
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
      @PathParam("namespace") @NotBlank String namespaceString,
      @Valid CreateNamespaceRequest request)
      throws ResourceException {
    try {
      final Namespace namespace =
          namespaceService.create(namespaceApiMapper.fromString(namespaceString, request));
      final NamespaceResponse response = NamespaceResponseMapper.map(namespace);
      return Response.ok(response).build();
    } catch (UnexpectedException e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/namespaces/{namespace}")
  public Response get(@PathParam("namespace") String namespaceString) throws ResourceException {
    try {
      final Optional<NamespaceResponse> namespaceResponse =
          namespaceService.get(namespaceString).map(NamespaceResponseMapper::map);
      if (namespaceResponse.isPresent()) {
        return Response.ok(namespaceResponse.get()).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (UnexpectedException e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/namespaces")
  public Response listNamespaces() throws ResourceException {
    try {
      final List<Namespace> namespaces = namespaceService.listNamespaces();
      final List<NamespaceResponse> namespaceResponses =
          coreNamespaceToApiNamespaceMapper.map(namespaces);
      return Response.ok(new NamespacesResponse(namespaceResponses)).build();
    } catch (UnexpectedException e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }
}
