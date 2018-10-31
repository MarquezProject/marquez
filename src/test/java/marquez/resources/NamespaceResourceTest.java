package marquez.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.Response;
import marquez.NamespaceBaseTest;
import marquez.api.GetNamespaceResponse;
import marquez.api.ListNamespacesResponse;
import marquez.core.exceptions.NamespaceException;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;
import marquez.core.models.Namespace;
import marquez.core.services.NamespaceService;
import org.junit.Test;
import org.mockito.Mockito;

public class NamespaceResourceTest extends NamespaceBaseTest {

  CoreNamespaceToApiNamespaceMapper namespaceMapper = new CoreNamespaceToApiNamespaceMapper();

  @Test
  public void testCreateNamespaceRequestErrorHandling() throws Exception {
    NamespaceService namespaceService = Mockito.mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);
    when(namespaceService.create(anyString(), anyString(), anyString()))
        .thenThrow(NamespaceException.class);

    Response res = namespaceResource.create("someNamespace", createNamespaceRequest);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testValidNamespace() throws NamespaceException {
    Optional<Namespace> returnedOptionalNamespace = Optional.of(TEST_NAMESPACE);
    NamespaceService namespaceService = Mockito.mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);

    when(namespaceService.get(NAMESPACE_NAME)).thenReturn(returnedOptionalNamespace);
    Response res = namespaceResource.get(NAMESPACE_NAME);
    GetNamespaceResponse responseBody = (GetNamespaceResponse) res.getEntity();

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    assertThat(responseBody.getNamespace().getName()).isEqualTo(NAMESPACE_NAME);
    assertThat(responseBody.getNamespace().getDescription()).isEqualTo(DESCRIPTION);
    assertThat(responseBody.getNamespace().getOwnerName()).isEqualTo(OWNER);
  }

  @Test
  public void testGetNamespaceRequestErrorHandling() throws Exception {
    NamespaceService namespaceService = Mockito.mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);
    when(namespaceService.get(anyString())).thenThrow(NamespaceException.class);

    Response res = namespaceResource.get("someNamespace");
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testListNamespaceWithSingleResultSet() throws NamespaceException {
    final List<Namespace> existingCoreModelNamespaces = Collections.singletonList(TEST_NAMESPACE);
    NamespaceService namespaceService = Mockito.mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);
    when(namespaceService.listNamespaces()).thenReturn(existingCoreModelNamespaces);

    Response res = namespaceResource.listNamespaces();
    ListNamespacesResponse responseBody = (ListNamespacesResponse) res.getEntity();

    marquez.api.Namespace expectedApiNamespace = namespaceMapper.map(TEST_NAMESPACE).get();
    assertThat(responseBody.getNamespaces()).contains(expectedApiNamespace);
  }

  @Test
  public void testAllNamespaceFieldsPresentInListNamespacesResponse() throws NamespaceException {
    final List<Namespace> existingNamespaces = Collections.singletonList(TEST_NAMESPACE);
    NamespaceService namespaceService = Mockito.mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);

    when(namespaceService.listNamespaces()).thenReturn(existingNamespaces);
    Response res = namespaceResource.listNamespaces();

    ListNamespacesResponse responseBody = (ListNamespacesResponse) res.getEntity();
    marquez.api.Namespace nsResponseFromList = responseBody.getNamespaces().get(0);

    assertThat(nsResponseFromList.getName()).isEqualTo(TEST_NAMESPACE.getName());
    assertThat(nsResponseFromList.getOwnerName()).isEqualTo(TEST_NAMESPACE.getOwnerName());
    assertThat(nsResponseFromList.getDescription()).isEqualTo(TEST_NAMESPACE.getDescription());
  }

  @Test
  public void testListNamespaceWithMultipleResultSet() throws NamespaceException {
    NamespaceService namespaceService = Mockito.mock(NamespaceService.class);
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
    marquez.api.Namespace nsResponse = namespaceMapper.map(TEST_NAMESPACE).get();
    marquez.api.Namespace secondNsResponse = namespaceMapper.map(secondNamespace).get();

    assertThat(responseBody.getNamespaces()).containsExactly(nsResponse, secondNsResponse);
  }

  @Test
  public void testListNamespacesErrorHandling() throws NamespaceException {
    NamespaceService namespaceService = Mockito.mock(NamespaceService.class);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);
    when(namespaceService.listNamespaces()).thenThrow(NamespaceException.class);

    Response res = namespaceResource.listNamespaces();
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }
}
