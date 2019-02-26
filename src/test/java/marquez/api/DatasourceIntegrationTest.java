package marquez.api;

import marquez.api.models.DatasourcesResponse;
import marquez.api.models.NamespacesResponse;
import marquez.db.fixtures.AppWithPostgresRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class DatasourceIntegrationTest {

    @ClassRule
    public static final AppWithPostgresRule APP = new AppWithPostgresRule();


    @Test
    public void testListDatasourcesWithEmptyResultSet() {
        final Response res =
                APP.client()
                        .target(URI.create("http://localhost:" + APP.getLocalPort()))
                        .path("/api/v1/datasources")
                        .request(MediaType.APPLICATION_JSON)
                        .get();
        assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

        DatasourcesResponse responseBody = res.readEntity(DatasourcesResponse.class);
        assertThat(responseBody.getDatasources().isEmpty());
    }
}
