package marquez.api.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import marquez.api.models.GetNamespaceResponse;
import marquez.api.models.ListNamespacesResponse;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;
import marquez.core.mappers.ResourceExceptionMapper;
import marquez.core.models.Namespace;
import marquez.core.services.NamespaceService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class NamespaceResourceTest extends NamespaceBaseTest {

  CoreNamespaceToApiNamespaceMapper namespaceMapper = new CoreNamespaceToApiNamespaceMapper();
  private static final NamespaceService NAMESPACE_SERVICE = mock(NamespaceService.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(new NamespaceResource(NAMESPACE_SERVICE))
          .addProvider(ResourceExceptionMapper.class)
          .build();

  @Before
  public void clearMocks() {
    reset(NAMESPACE_SERVICE);
  }

  @Test
  public void testCreateNamespaceErrorHandling() throws UnexpectedException {
    doThrow(new UnexpectedException()).when(NAMESPACE_SERVICE).create(any(Namespace.class));

    assertEquals(
        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
        resources
            .target("/api/v1/namespaces/" + NAMESPACE_NAME)
            .request()
            .put(Entity.json(createNamespaceRequest))
            .getStatus());
  }

  @Test
  public void testValidNamespace() throws ResourceException, UnexpectedException {
    Optional<Namespace> returnedOptionalNamespace = Optional.of(TEST_NAMESPACE);
    NamespaceService namespaceService = mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);

    when(namespaceService.get(NAMESPACE_NAME)).thenReturn(returnedOptionalNamespace);
    Response res = namespaceResource.get(NAMESPACE_NAME);
    GetNamespaceResponse responseBody = (GetNamespaceResponse) res.getEntity();

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    assertThat(responseBody.getNamespace().getName()).isEqualTo(NAMESPACE_NAME);
    assertThat(responseBody.getNamespace().getDescription()).isEqualTo(DESCRIPTION);
    assertThat(responseBody.getNamespace().getOwner()).isEqualTo(OWNER);
  }

  @Test
  public void testGetNamespaceRequestErrorHandling() throws Throwable {
    doThrow(new UnexpectedException()).when(NAMESPACE_SERVICE).get(any());

    assertEquals(
        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
        resources.target("/api/v1/namespaces/" + NAMESPACE_NAME).request().get().getStatus());
  }

  @Test
  public void testListNamespaceWithSingleResultSet() throws UnexpectedException, ResourceException {
    final List<Namespace> existingCoreModelNamespaces = Collections.singletonList(TEST_NAMESPACE);
    NamespaceService namespaceService = mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);
    when(namespaceService.listNamespaces()).thenReturn(existingCoreModelNamespaces);

    Response res = namespaceResource.listNamespaces();
    ListNamespacesResponse responseBody = (ListNamespacesResponse) res.getEntity();

    marquez.api.models.Namespace expectedApiNamespace = namespaceMapper.map(TEST_NAMESPACE);
    assertThat(responseBody.getNamespaces()).contains(expectedApiNamespace);
  }

  @Test
  public void testAllNamespaceFieldsPresentInListNamespacesResponse()
      throws UnexpectedException, ResourceException {
    final List<Namespace> existingNamespaces = Collections.singletonList(TEST_NAMESPACE);
    NamespaceService namespaceService = mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);

    when(namespaceService.listNamespaces()).thenReturn(existingNamespaces);
    Response res = namespaceResource.listNamespaces();

    ListNamespacesResponse responseBody = (ListNamespacesResponse) res.getEntity();
    marquez.api.models.Namespace nsResponseFromList = responseBody.getNamespaces().get(0);

    assertThat(nsResponseFromList.getName()).isEqualTo(TEST_NAMESPACE.getName());
    assertThat(nsResponseFromList.getOwner()).isEqualTo(TEST_NAMESPACE.getOwnerName());
    assertThat(nsResponseFromList.getDescription()).isEqualTo(TEST_NAMESPACE.getDescription());
  }

  @Test
  public void testListNamespaceWithMultipleResultSet()
      throws UnexpectedException, ResourceException {
    NamespaceService namespaceService = mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);

    final List<Namespace> existingCoreModelNamespaces = new ArrayList<>();
    Namespace secondNamespace =
        new Namespace(
            UUID.randomUUID(),
            Timestamp.from(Instant.now()),
            "someOtherName",
            "someOtherOwner",
            "a second ns for testing");
    existingCoreModelNamespaces.add(TEST_NAMESPACE);
    existingCoreModelNamespaces.add(secondNamespace);
    when(namespaceService.listNamespaces()).thenReturn(existingCoreModelNamespaces);

    Response res = namespaceResource.listNamespaces();
    ListNamespacesResponse responseBody = (ListNamespacesResponse) res.getEntity();
    marquez.api.models.Namespace nsResponse = namespaceMapper.map(TEST_NAMESPACE);
    marquez.api.models.Namespace secondNsResponse = namespaceMapper.map(secondNamespace);

    assertThat(responseBody.getNamespaces()).containsExactly(nsResponse, secondNsResponse);
  }

  @Test
  public void testListNamespacesErrorHandling() throws UnexpectedException {
    doThrow(new UnexpectedException()).when(NAMESPACE_SERVICE).listNamespaces();

    assertEquals(
        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
        resources.target("/api/v1/namespaces/").request().get().getStatus());
  }
}
