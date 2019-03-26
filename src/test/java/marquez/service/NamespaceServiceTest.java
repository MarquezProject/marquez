package marquez.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.NamespaceName;
import marquez.db.NamespaceDao;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Generator;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.After;
import org.junit.Test;

public class NamespaceServiceTest {
  private final NamespaceName NAMESPACE_NAME = NamespaceName.fromString("test");

  private static final NamespaceDao namespaceDao = mock(NamespaceDao.class);
  NamespaceService namespaceService = new NamespaceService(namespaceDao);

  @After
  public void teardown() {
    reset(namespaceDao);
  }

  @Test
  public void testCreate() throws MarquezServiceException {
    Namespace ns = Generator.genNamespace();
    final NamespaceName namespaceName = NamespaceName.fromString(ns.getName());
    when(namespaceDao.findBy(namespaceName))
        .thenReturn(
            Optional.of(
                new Namespace(
                    UUID.randomUUID(), ns.getName(), ns.getOwnerName(), ns.getDescription())));
    Namespace nsReturned = namespaceService.create(ns);
    verify(namespaceDao).insert(any(Namespace.class));
    assertEquals(ns.getName(), nsReturned.getName());
    assertEquals(ns.getOwnerName(), nsReturned.getOwnerName());
    assertEquals(ns.getDescription(), nsReturned.getDescription());
  }

  @Test(expected = MarquezServiceException.class)
  public void testCreate_findException() throws MarquezServiceException {
    Namespace ns = Generator.genNamespace();
    doThrow(UnableToExecuteStatementException.class).when(namespaceDao).findBy(NAMESPACE_NAME);
    namespaceService.create(ns);
  }

  @Test(expected = MarquezServiceException.class)
  public void testCreate_insertException() throws MarquezServiceException {
    Namespace ns = Generator.genNamespace();
    doThrow(UnableToExecuteStatementException.class)
        .when(namespaceDao)
        .insert(any(Namespace.class));
    namespaceService.create(ns);
  }

  @Test
  public void testExists() throws MarquezServiceException {
    final NamespaceName found = NamespaceName.fromString("test0");
    final NamespaceName notFound = NamespaceName.fromString("test1");

    when(namespaceDao.exists(found)).thenReturn(true);
    assertTrue(namespaceService.exists(found));
    when(namespaceDao.exists(notFound)).thenReturn(false);
    assertFalse(namespaceService.exists(notFound));
  }

  @Test(expected = MarquezServiceException.class)
  public void testExists_findException() throws MarquezServiceException {
    doThrow(UnableToExecuteStatementException.class).when(namespaceDao).exists(NAMESPACE_NAME);
    namespaceService.exists(NAMESPACE_NAME);
  }

  @Test
  public void testGet_NsExists() throws MarquezServiceException {
    Namespace ns = Generator.genNamespace();
    final NamespaceName namespaceName = NamespaceName.fromString(ns.getName());
    when(namespaceDao.findBy(namespaceName)).thenReturn(Optional.of(ns));
    Optional<Namespace> nsOptional = namespaceService.get(namespaceName);
    assertTrue(nsOptional.isPresent());
  }

  @Test
  public void testGet_NsNotFound() throws MarquezServiceException {
    Namespace ns = Generator.genNamespace();
    final NamespaceName namespaceName = NamespaceName.fromString(ns.getName());
    when(namespaceDao.findBy(namespaceName)).thenReturn(Optional.empty());
    Optional<Namespace> namespaceIfFound = namespaceService.get(namespaceName);
    assertFalse(namespaceIfFound.isPresent());
  }

  @Test(expected = MarquezServiceException.class)
  public void testGet_findException() throws MarquezServiceException {
    doThrow(UnableToExecuteStatementException.class).when(namespaceDao).findBy(NAMESPACE_NAME);
    namespaceService.get(NAMESPACE_NAME);
  }

  @Test
  public void testListNamespaces() throws MarquezServiceException {
    List<Namespace> namespaces =
        new ArrayList<Namespace>(Arrays.asList(Generator.genNamespace(), Generator.genNamespace()));
    when(namespaceDao.findAll()).thenReturn(namespaces);
    List<Namespace> namespacesFound = namespaceService.getAll();
    assertEquals(namespaces.size(), namespacesFound.size());
  }

  @Test
  public void testListNamespaces_Empty() throws MarquezServiceException {
    when(namespaceDao.findAll()).thenReturn(new ArrayList<Namespace>());
    List<Namespace> namespacesFound = namespaceService.getAll();
    assertEquals(0, namespacesFound.size());
  }

  @Test(expected = MarquezServiceException.class)
  public void testListNamespaces_findAllException() throws MarquezServiceException {
    doThrow(UnableToExecuteStatementException.class).when(namespaceDao).findAll();
    namespaceService.getAll();
  }
}
