package marquez.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import marquez.api.NuHealthCheckResource;
import marquez.service.ServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

@ExtendWith(DropwizardExtensionsSupport.class)
public class NuHealthCheckResourceTest extends BaseResourceIntegrationTest {

  @Mock private ServiceFactory serviceFactory;

  private NuHealthCheckResource nuHealthCheckResource;

  @BeforeEach
  public void setUp() {
    nuHealthCheckResource = new NuHealthCheckResource(serviceFactory);
  }

  @Test
  void testGet() {
    Response response = nuHealthCheckResource.get();
    assertEquals(response.getStatus(),Response.Status.OK.getStatusCode());
  }
}
