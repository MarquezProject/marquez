package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import marquez.api.CreateNamespaceRequest;
import marquez.api.CreateNamespaceResponse;
import marquez.api.ListNamespacesResponse;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;
import marquez.core.mappers.GetNamespaceResponseMapper;
import marquez.core.mappers.NamespaceApiMapper;
import marquez.core.models.Namespace;
import marquez.core.services.NamespaceService;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/namespaces")
@Produces(APPLICATION_JSON)
public class NamespaceResource extends BaseResource {
  private static final Logger LOG = LoggerFactory.getLogger(NamespaceResource.class);

  private final NamespaceService namespaceService;
  private final GetNamespaceResponseMapper getNamespaceResponseMapper =
      new GetNamespaceResponseMapper();

  private final CoreNamespaceToApiNamespaceMapper namespaceMapper =
      new CoreNamespaceToApiNamespaceMapper();

  private final NamespaceApiMapper namespaceAPIMapper = new NamespaceApiMapper();

  public NamespaceResource(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{namespace}")
  public Response create(
      @PathParam("namespace") @NotEmpty String namespace, @Valid CreateNamespaceRequest request)
      throws ResourceException {
    try {
      marquez.core.models.Namespace n =
          namespaceService.create(namespaceAPIMapper.of(namespace, request));
      return Response.status(Response.Status.OK)
          .entity(new CreateNamespaceResponse(namespaceMapper.map(n)))
          .type(APPLICATION_JSON)
          .build();
    } catch (UnexpectedException e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{namespace}")
  public Response get(@PathParam("namespace") String namespace) throws ResourceException {
    try {
      Optional<Namespace> n = namespaceService.get(namespace);
      if (n.isPresent()) {
        Namespace returnedNamespace = n.get();
        return Response.status(Response.Status.OK)
            .entity(getNamespaceResponseMapper.map(returnedNamespace))
            .type(APPLICATION_JSON)
            .build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).type(APPLICATION_JSON).build();
      }
    } catch (UnexpectedException e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response listNamespaces() throws ResourceException {
    try {
      List<marquez.core.models.Namespace> namespaceCoreModels = namespaceService.listNamespaces();
      List<marquez.api.Namespace> namespaceList = new CoreNamespaceToApiNamespaceMapper().map(namespaceCoreModels);
      return Response.status(Response.Status.OK)
              .entity(new ListNamespacesResponse(namespaceList))
              .build();
    } catch (UnexpectedException e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }
}
