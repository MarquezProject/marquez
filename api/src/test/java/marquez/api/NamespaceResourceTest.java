package marquez.api;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.NamespaceDao;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
class NamespaceResourceTest {
  private static NamespaceDao namespaceDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
  }

  @Test
  void testFindAllFilter() {
    var namespaceName1 = NamespaceName.of("postgres://localhost:5432");
    var namespaceMeta1 = new NamespaceMeta(new OwnerName("marquez"), null);
    namespaceDao.upsertNamespaceMeta(namespaceName1, namespaceMeta1);

    var namespaceName2 = NamespaceName.of("excluded_namespace");
    var namespaceMeta2 = new NamespaceMeta(new OwnerName("yannick"), null);
    namespaceDao.upsertNamespaceMeta(namespaceName2, namespaceMeta2);

    List<Namespace> namespaces = namespaceDao.findAllFilter("excluded.*", 10, 0);

    // Assert that the namespaces list does not contain the excluded namespace
    assertFalse(
        namespaces.stream().anyMatch(namespace -> namespace.getName().equals(namespaceName2)));
  }
}
