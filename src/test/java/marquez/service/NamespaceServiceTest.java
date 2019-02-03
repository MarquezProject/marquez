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
import marquez.db.NamespaceDao;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.Generator;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.After;
import org.junit.Test;

public class NamespaceServiceTest {
  private static final NamespaceDao namespaceDao = mock(NamespaceDao.class);
  NamespaceService namespaceService = new NamespaceService(namespaceDao);

  @After
  public void teardown() {
    reset(namespaceDao);
  }

  @Test
  public void testCreate() throws UnexpectedException {
    Namespace ns = Generator.genNamespace();
    when(namespaceDao.find(any(String.class)))
        .thenReturn(
            new Namespace(UUID.randomUUID(), ns.getName(), ns.getOwnerName(), ns.getDescription()));
    Namespace nsReturned = namespaceService.create(ns);
    verify(namespaceDao).insert(any(Namespace.class));
    assertEquals(ns.getName(), nsReturned.getName());
    assertEquals(ns.getOwnerName(), nsReturned.getOwnerName());
    assertEquals(ns.getDescription(), nsReturned.getDescription());
  }

  @Test(expected = UnexpectedException.class)
  public void testCreate_findException() throws UnexpectedException {
    Namespace ns = Generator.genNamespace();
    doThrow(UnableToExecuteStatementException.class).when(namespaceDao).find(any(String.class));
    namespaceService.create(ns);
  }

  @Test(expected = UnexpectedException.class)
  public void testCreate_insertException() throws UnexpectedException {
    Namespace ns = Generator.genNamespace();
    doThrow(UnableToExecuteStatementException.class)
        .when(namespaceDao)
        .insert(any(Namespace.class));
    namespaceService.create(ns);
  }

  @Test
  public void testExists() throws UnexpectedException {
    when(namespaceDao.exists("ns_exists")).thenReturn(true);
    assertTrue(namespaceService.exists("ns_exists"));
    when(namespaceDao.exists("ns_doesnt_exist")).thenReturn(false);
    assertFalse(namespaceService.exists("ns_doesnt_exist"));
  }

  @Test(expected = UnexpectedException.class)
  public void testExists_findException() throws UnexpectedException {
    Namespace ns = Generator.genNamespace();
    doThrow(UnableToExecuteStatementException.class).when(namespaceDao).exists(any(String.class));
    namespaceService.exists(ns.getName());
  }

  @Test
  public void testGet_NsExists() throws UnexpectedException {
    Namespace ns = Generator.genNamespace();
    when(namespaceDao.find(ns.getName())).thenReturn(ns);
    Optional<Namespace> nsOptional = namespaceService.get(ns.getName());
    assertTrue(nsOptional.isPresent());
  }

  @Test
  public void testGet_NsNotFound() throws UnexpectedException {
    Namespace ns = Generator.genNamespace();
    when(namespaceDao.find(ns.getName())).thenReturn(null);
    Optional<Namespace> nsOptional = namespaceService.get(ns.getName());
    assertFalse(nsOptional.isPresent());
  }

  @Test(expected = UnexpectedException.class)
  public void testGet_findException() throws UnexpectedException {
    Namespace ns = Generator.genNamespace();
    doThrow(UnableToExecuteStatementException.class).when(namespaceDao).find(any(String.class));
    namespaceService.get(ns.getName());
  }

  @Test
  public void testListNamespaces() throws UnexpectedException {
    List<Namespace> namespaces =
        new ArrayList<Namespace>(Arrays.asList(Generator.genNamespace(), Generator.genNamespace()));
    when(namespaceDao.findAll()).thenReturn(namespaces);
    List<Namespace> namespacesFound = namespaceService.listNamespaces();
    assertEquals(namespaces.size(), namespacesFound.size());
  }

  @Test
  public void testListNamespaces_Empty() throws UnexpectedException {
    when(namespaceDao.findAll()).thenReturn(new ArrayList<Namespace>());
    List<Namespace> namespacesFound = namespaceService.listNamespaces();
    assertEquals(0, namespacesFound.size());
  }

  @Test(expected = UnexpectedException.class)
  public void testListNamespaces_findAllException() throws UnexpectedException {
    doThrow(UnableToExecuteStatementException.class).when(namespaceDao).findAll();
    namespaceService.listNamespaces();
  }
}
