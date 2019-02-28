package marquez.api;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.api.models.DatasourceResponse;
import marquez.api.models.DatasourcesResponse;
import marquez.db.DatasourceDao;
import marquez.db.fixtures.AppWithPostgresRule;
import marquez.service.DatasourceService;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.Datasource;
import marquez.service.models.Generator;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class DatasourceIntegrationTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  protected static DatasourceDao datasourceDao;
  protected static DatasourceService datasourceService;

  @BeforeClass
  public static void setup() {
    datasourceDao = APP.onDemand(DatasourceDao.class);
    datasourceService = new DatasourceService(datasourceDao);
  }

  @After
  public void deleteDatasources() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(format("delete from datasources"));
            });
  }

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

  @Test
  public void testListDatasourceResponseCorrect() throws UnexpectedException {
    Datasource ds1 = Generator.genDatasource();

    datasourceService.create(ds1.getConnectionUrl(), ds1.getDatasourceName());

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/datasources")
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    DatasourcesResponse responseBody = res.readEntity(DatasourcesResponse.class);
    assertThat(responseBody.getDatasources().size()).isEqualTo(1);
    DatasourceResponse returnedDatasource = responseBody.getDatasources().get(0);

    assertThat(returnedDatasource.getName()).isEqualTo(ds1.getDatasourceName().getValue());
    System.out.println(returnedDatasource.getConnectionUrl());
    assertThat(returnedDatasource.getConnectionUrl())
        .isEqualTo(ds1.getConnectionUrl().getRawValue());
  }

  @Test
  public void testListDatasourcesWithMultipleResults() throws UnexpectedException {
    Datasource ds1 = Generator.genDatasource();
    Datasource ds2 = Generator.genDatasource();

    datasourceService.create(ds1.getConnectionUrl(), ds1.getDatasourceName());
    datasourceService.create(ds2.getConnectionUrl(), ds2.getDatasourceName());

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/datasources")
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    DatasourcesResponse responseBody = res.readEntity(DatasourcesResponse.class);
    assertThat(responseBody.getDatasources().size()).isEqualTo(2);
  }
}
