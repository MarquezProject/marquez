package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.sql.Timestamp;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.NamespaceBaseTest;
import marquez.dao.fixtures.AppWithPostgresRule;
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

    Namespace responseBody = res.readEntity(Namespace.class);

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
    Namespace responseBody = res.readEntity(Namespace.class);
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
            .put(Entity.json(new CreateNamespaceRequest("someOwner", null)));
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

    ListNamespacesResponse responseBody = res.readEntity(ListNamespacesResponse.class);
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
