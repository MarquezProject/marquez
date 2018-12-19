package marquez.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import marquez.core.models.Generator;
import marquez.core.models.Namespace;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

public class NamespaceDAOTest {
  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();
  NamespaceDAO namespaceDAO = APP.onDemand(NamespaceDAO.class);

  @After
  public void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM namespaces;");
            });
  }

  private void assertFieldsMatchExceptTS(Namespace ns1, Namespace ns2) {
    assertEquals(ns1.getGuid(), ns2.getGuid());
    assertEquals(ns1.getName(), ns2.getName());
    assertEquals(ns1.getDescription(), ns2.getDescription());
    assertEquals(ns1.getOwnerName(), ns2.getOwnerName());
  }

  @Test
  public void testFind() {
    Namespace namespace = Generator.genNamespace();
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  "INSERT INTO namespaces(guid, name, description, current_ownership) VALUES (?, ?, ?, ?);",
                  namespace.getGuid(),
                  namespace.getName(),
                  namespace.getDescription(),
                  namespace.getOwnerName());
            });
    assertFieldsMatchExceptTS(namespace, namespaceDAO.find(namespace.getName()));
    assertEquals(null, namespaceDAO.find("nonexistent_namespace"));
  }

  @Test
  public void testInsert() {
    Namespace newNs = Generator.genNamespace();
    namespaceDAO.insert(newNs);
    assertFieldsMatchExceptTS(newNs, namespaceDAO.find(newNs.getName()));
  }

  @Test
  public void testFindAll() {
    List<Namespace> existingNamespacesFound = namespaceDAO.findAll();
    List<Namespace> newNamespaces =
        new ArrayList<Namespace>(Arrays.asList(Generator.genNamespace(), Generator.genNamespace()));
    newNamespaces.forEach(n -> namespaceDAO.insert(n));
    List<Namespace> namespacesFound = namespaceDAO.findAll();
    assertEquals(newNamespaces.size(), namespacesFound.size() - existingNamespacesFound.size());
  }

  @Test
  public void testFindAll_Empty() {
    assertEquals(0, namespaceDAO.findAll().size());
  }

  @Test
  public void testExists() {
    Namespace nonexistentNs = Generator.genNamespace();
    assertFalse(namespaceDAO.exists(nonexistentNs.getName()));
    Namespace existingNs = Generator.genNamespace();
    namespaceDAO.insert(existingNs);
    assertTrue(namespaceDAO.exists(existingNs.getName()));
  }
}
