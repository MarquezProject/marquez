package marquez.api.resources;

import static java.time.Instant.now;

import java.sql.Timestamp;
import java.util.UUID;
import marquez.api.models.CreateNamespaceRequest;
import marquez.service.models.Namespace;
import org.junit.BeforeClass;

public class NamespaceBaseTest {

  public static final UUID NAMESPACE_UUID = UUID.randomUUID();
  public static final String OWNER = "someOwner";
  public static final String DESCRIPTION = "someDescription";
  public static final Timestamp START_TIME = Timestamp.from(now());
  public static final String NAMESPACE_NAME = "someNamespace";
  public static final Namespace TEST_NAMESPACE =
      new Namespace(NAMESPACE_UUID, START_TIME, NAMESPACE_NAME, OWNER, DESCRIPTION);

  public static CreateNamespaceRequest createNamespaceRequest;

  public final int HTTP_UNPROCESSABLE_ENTITY = 422;

  @BeforeClass
  public static void setup() {
    createNamespaceRequest = new CreateNamespaceRequest(OWNER, DESCRIPTION);
  }
}
