package marquez.api;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.ClassRule;
import org.junit.Test;

public class NamespaceIntegrationTest {
  protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  static final String OWNER = "someOwner";
  static final String DESCRIPTION = "someDescription";
  static final String NAMESPACE_NAME = "someNamespace";

  static final Timestamp START_TIME = Timestamp.from(now());
  static Entity createNamespaceRequestEntity;

  {
    try {
      createNamespaceRequestEntity =
          Entity.json(MAPPER.writeValueAsString(new CreateNamespaceRequest(OWNER, DESCRIPTION)));
    } catch (JsonProcessingException e) {
      fail("Could not construct test object for the request entity.");
    }
  }

  @Test
  public void testCreateNamespace() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/namespaces/" + NAMESPACE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .put(createNamespaceRequestEntity);
    assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), res.getStatus());

    CreateNamespaceResponse responseBody = res.readEntity(CreateNamespaceResponse.class);
    String responseOwner = responseBody.getOwner();
    String responseDescription = responseBody.getDescription();
    Timestamp responseTimeStamp = responseBody.getCreatedAt();

    assertThat(responseTimeStamp.after(START_TIME));
    assertThat(responseOwner).isEqualTo(OWNER);
    assertThat(responseDescription).isEqualTo(DESCRIPTION);
  }

  @Test
  public void testGetAllNamespaces() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/namespaces/")
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), res.getStatus());

    GetAllNamespacesResponse responseBody = res.readEntity(GetAllNamespacesResponse.class);
    List<Namespace> returnedList = responseBody.getNamespaces();

    assertThat(returnedList.isEmpty());
  }

  @Test
  public void testGetAllNamespacesWithoutTrailingSlash() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/namespaces")
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), res.getStatus());

    GetAllNamespacesResponse responseBody = res.readEntity(GetAllNamespacesResponse.class);
    List<Namespace> returnedList = responseBody.getNamespaces();

    assertThat(returnedList.isEmpty());
  }

  @Test
  public void tesGetNamespaceNoSuchNamespace() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/namespaces/" + "nosuchnamespace")
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), res.getStatus());
  }

  @Test
  public void testCreateNamespaceInvalidUri() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/namespace/" + NAMESPACE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .put(createNamespaceRequestEntity);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }
}
