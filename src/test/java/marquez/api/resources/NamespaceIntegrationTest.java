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

import java.net.URI;
import java.sql.Timestamp;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.api.models.NamespaceRequest;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.db.fixtures.AppWithPostgresRule;
import org.junit.ClassRule;
import org.junit.Test;

public class NamespaceIntegrationTest extends NamespaceBaseTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  @Test
  public void testCreateNamespace() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespaces/" + NAMESPACE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(createNamespaceRequest));
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    NamespaceResponse responseBody = res.readEntity(NamespaceResponse.class);

    assertThat(Timestamp.valueOf(responseBody.getCreatedAt())).isAfter(START_TIME);
    assertThat(responseBody.getOwner()).isEqualTo(OWNER);
    assertThat(responseBody.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  public void testCreateNamespace_NoDup() {
    APP.client()
        .target(URI.create("http://localhost:" + APP.getLocalPort()))
        .path("/api/v1/namespaces/" + NAMESPACE_NAME)
        .request(MediaType.APPLICATION_JSON)
        .put(Entity.json(createNamespaceRequest));
    Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespaces/" + NAMESPACE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(createNamespaceRequest));
    NamespaceResponse responseBody = res.readEntity(NamespaceResponse.class);
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    assertThat(Timestamp.valueOf(responseBody.getCreatedAt())).isAfter(START_TIME);
    assertThat(responseBody.getOwner()).isEqualTo(OWNER);
    assertThat(responseBody.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  public void testBadCreateNamespaceRequest() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespaces/" + "abc123")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(new NamespaceRequest(null, "someDesc")));
    assertEquals(HTTP_UNPROCESSABLE_ENTITY, res.getStatus());
  }

  @Test
  public void testListNamespaceWithEmptyResultSet() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespaces/")
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    NamespacesResponse responseBody = res.readEntity(NamespacesResponse.class);
    assertThat(responseBody.getNamespaces().isEmpty());
  }

  @Test
  public void testGetNamespaceNoSuchNamespace() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespaces/" + "nosuchnamespace")
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testCreateNamespaceInvalidUri() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespace/" + NAMESPACE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(createNamespaceRequest));
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }
}
