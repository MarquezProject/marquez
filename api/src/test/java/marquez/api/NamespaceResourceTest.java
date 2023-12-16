package marquez.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.ws.rs.core.Response;
import marquez.api.NamespaceResource.Namespaces;
import marquez.api.filter.exclusions.Exclusions;
import marquez.api.filter.exclusions.ExclusionsConfig;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.BaseDao;
import marquez.db.NamespaceDao;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.NamespaceService;
import marquez.service.ServiceFactory;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class NamespaceResourceTest {

  @Mock private ServiceFactory serviceFactory;

  @Mock private BaseDao baseDao;

  private static NamespaceDao namespaceDao;
  private NamespaceService namespaceService;
  private NamespaceResource namespaceResource;

  @BeforeEach
  public void setUp(Jdbi jdbi) {
    MockitoAnnotations.openMocks(this);

    namespaceDao = jdbi.onDemand(NamespaceDao.class);
    when(baseDao.createNamespaceDao()).thenReturn(namespaceDao);
    namespaceService = new NamespaceService(baseDao);

    when(serviceFactory.getNamespaceService()).thenReturn(namespaceService);
    namespaceResource = new NamespaceResource(serviceFactory);
  }

  @Test
  void testFindAllFilter() {
    var namespaceName1 = NamespaceName.of("postgres://localhost:5432");
    var namespaceMeta1 = new NamespaceMeta(new OwnerName("marquez"), null);
    namespaceDao.upsertNamespaceMeta(namespaceName1, namespaceMeta1);

    var namespaceName2 = NamespaceName.of("excluded_namespace");
    var namespaceMeta2 = new NamespaceMeta(new OwnerName("yannick"), null);
    namespaceDao.upsertNamespaceMeta(namespaceName2, namespaceMeta2);

    List<Namespace> namespaces = namespaceDao.findAllWithExclusion("excluded.*", 10, 0);

    // Assert that the namespaces list does not contain the excluded namespace
    assertFalse(
        namespaces.stream().anyMatch(namespace -> namespace.getName().equals(namespaceName2)));
  }

  @Test
  public void testListWithFilter() {
    String filter = "excluded_.*";
    ExclusionsConfig exclusionsConfig = new ExclusionsConfig();
    ExclusionsConfig.NamespaceExclusions namespaceExclusions =
        new ExclusionsConfig.NamespaceExclusions();
    ExclusionsConfig.OnRead onRead = new ExclusionsConfig.OnRead();
    onRead.enabled = true;
    onRead.pattern = filter;
    namespaceExclusions.onRead = onRead;

    exclusionsConfig.namespaces = namespaceExclusions;
    Exclusions.use(exclusionsConfig);

    NamespaceName namespaceName = NamespaceName.of("excluded_namespace");
    OwnerName owner = new OwnerName("yannick");
    NamespaceMeta namespaceMeta = new NamespaceMeta(owner, "description");

    namespaceDao.upsertNamespaceMeta(namespaceName, namespaceMeta);

    NamespaceService namespaceServiceSpy = spy(namespaceService);
    doCallRealMethod()
        .when(namespaceServiceSpy)
        .findAllWithExclusion(eq(filter), anyInt(), anyInt());

    Response response = namespaceResource.list(10, 0);
    Namespaces namespaces = (Namespaces) response.getEntity();

    // Check if the returned namespaces contain a namespace with the name
    // "excluded_namespace"
    boolean containsExcludedNamespace =
        namespaces.getValue().stream()
            .anyMatch(namespace -> namespace.getName().getValue().equals("excluded_namespace"));

    // Assert that the returned namespaces do not contain a namespace with the name
    // "excluded_namespace"
    assertFalse(containsExcludedNamespace);
  }
}
