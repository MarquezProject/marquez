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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import marquez.api.exceptions.MarquezServiceExceptionMapper;
import marquez.api.mappers.NamespaceResponseMapper;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.common.models.NamespaceName;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Namespace;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

public class NamespaceResourceTest extends NamespaceBaseTest {

  @Rule public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock private NamespaceService namespaceService;

  private NamespaceName namespaceName = NamespaceName.of(NAMESPACE_NAME);

  private static final NamespaceService NAMESPACE_SERVICE = mock(NamespaceService.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(new NamespaceResource(NAMESPACE_SERVICE))
          .addProvider(MarquezServiceExceptionMapper.class)
          .build();

  @Before
  public void clearMocks() {
    reset(NAMESPACE_SERVICE);
  }

  @Test
  public void testCreateNamespaceErrorHandling() throws MarquezServiceException {
    doThrow(new MarquezServiceException())
        .when(NAMESPACE_SERVICE)
        .createOrUpdate(any(Namespace.class));

    assertEquals(
        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
        resources
            .target("/api/v1/namespaces/" + NAMESPACE_NAME)
            .request()
            .put(Entity.json(createNamespaceRequest))
            .getStatus());
  }

  @Test
  public void testValidNamespace() throws MarquezServiceException {
    Optional<Namespace> returnedOptionalNamespace = Optional.of(TEST_NAMESPACE);

    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);

    when(namespaceService.get(namespaceName)).thenReturn(returnedOptionalNamespace);
    Response res = namespaceResource.get(namespaceName.getValue());
    NamespaceResponse responseBody = (NamespaceResponse) res.getEntity();

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    assertThat(responseBody.getName()).isEqualTo(NAMESPACE_NAME);
    assertThat(responseBody.getCreatedAt()).isNotEmpty();
    assertThat(responseBody.getOwnerName()).isEqualTo(OWNER);
    assertThat(responseBody.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  public void testGetNamespaceRequestErrorHandling() throws Throwable {
    doThrow(new MarquezServiceException()).when(NAMESPACE_SERVICE).get(any());

    assertEquals(
        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
        resources.target("/api/v1/namespaces/" + NAMESPACE_NAME).request().get().getStatus());
  }

  @Test
  public void testListNamespaceWithNoResults() throws MarquezServiceException {
    final List<Namespace> existingCoreModelNamespaces = Collections.emptyList();
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);
    when(namespaceService.getAll()).thenReturn(existingCoreModelNamespaces);

    Response res = namespaceResource.list();
    NamespacesResponse responseBody = (NamespacesResponse) res.getEntity();

    assertThat(responseBody.getNamespaces()).isEmpty();
  }

  @Test
  public void testListNamespaceWithSingleResultSet() throws MarquezServiceException {
    final List<Namespace> existingCoreModelNamespaces = Collections.singletonList(TEST_NAMESPACE);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);
    when(namespaceService.getAll()).thenReturn(existingCoreModelNamespaces);

    Response res = namespaceResource.list();
    NamespacesResponse responseBody = (NamespacesResponse) res.getEntity();

    NamespaceResponse expectedApiNamespace = NamespaceResponseMapper.map(TEST_NAMESPACE);
    assertThat(responseBody.getNamespaces()).contains(expectedApiNamespace);
  }

  @Test
  public void testAllNamespaceFieldsPresentInListNamespacesResponse()
      throws MarquezServiceException {
    final List<Namespace> existingNamespaces = Collections.singletonList(TEST_NAMESPACE);
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);

    when(namespaceService.getAll()).thenReturn(existingNamespaces);
    Response res = namespaceResource.list();

    NamespacesResponse responseBody = (NamespacesResponse) res.getEntity();
    NamespaceResponse nsResponseFromList = responseBody.getNamespaces().get(0);

    assertThat(nsResponseFromList.getName()).isEqualTo(TEST_NAMESPACE.getName());
    assertThat(nsResponseFromList.getOwnerName()).isEqualTo(TEST_NAMESPACE.getOwnerName());
    assertThat(nsResponseFromList.getDescription()).isEqualTo(TEST_NAMESPACE.getDescription());
  }

  @Test
  public void testListNamespaceWithMultipleResultSet() throws MarquezServiceException {
    NamespaceResource namespaceResource = new NamespaceResource(namespaceService);

    final List<Namespace> existingCoreModelNamespaces = new ArrayList<>();
    Namespace secondNamespace =
        new Namespace(
            UUID.randomUUID(),
            Instant.now(),
            "someOtherName",
            "someOtherOwner",
            "a second ns for testing");
    existingCoreModelNamespaces.add(TEST_NAMESPACE);
    existingCoreModelNamespaces.add(secondNamespace);
    when(namespaceService.getAll()).thenReturn(existingCoreModelNamespaces);

    Response res = namespaceResource.list();
    NamespacesResponse responseBody = (NamespacesResponse) res.getEntity();
    NamespaceResponse nsResponse = NamespaceResponseMapper.map(TEST_NAMESPACE);
    NamespaceResponse secondNsResponse = NamespaceResponseMapper.map(secondNamespace);

    assertThat(responseBody.getNamespaces()).containsExactly(nsResponse, secondNsResponse);
  }

  @Test
  public void testListNamespacesErrorHandling() throws MarquezServiceException {
    doThrow(new MarquezServiceException()).when(NAMESPACE_SERVICE).getAll();

    assertEquals(
        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
        resources.target("/api/v1/namespaces/").request().get().getStatus());
  }
}
