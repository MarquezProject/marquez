/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import marquez.common.models.NamespaceName;
import marquez.db.fixtures.AppWithPostgresRule;
import marquez.service.models.Generator;
import marquez.service.models.Namespace;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

public class NamespaceDaoTest {
  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();
  NamespaceDao namespaceDao = APP.onDemand(NamespaceDao.class);

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
    assertFieldsMatchExceptTS(
        namespace, namespaceDao.findBy(NamespaceName.fromString(namespace.getName())).get());
    assertEquals(
        null, namespaceDao.findBy(NamespaceName.fromString("nonexistent_namespace")).orElse(null));
  }

  @Test
  public void testInsert() {
    Namespace newNs = Generator.genNamespace();
    namespaceDao.insert(newNs);
    assertFieldsMatchExceptTS(
        newNs, namespaceDao.findBy(NamespaceName.fromString(newNs.getName())).get());
  }

  @Test
  public void testFindAll() {
    List<Namespace> existingNamespacesFound = namespaceDao.findAll();
    List<Namespace> newNamespaces =
        new ArrayList<Namespace>(Arrays.asList(Generator.genNamespace(), Generator.genNamespace()));
    newNamespaces.forEach(n -> namespaceDao.insert(n));
    List<Namespace> namespacesFound = namespaceDao.findAll();
    assertEquals(newNamespaces.size(), namespacesFound.size() - existingNamespacesFound.size());
  }

  @Test
  public void testFindAll_Empty() {
    assertEquals(0, namespaceDao.findAll().size());
  }

  @Test
  public void testExists() {
    final Namespace newNamespace = Generator.genNamespace();
    final NamespaceName found = NamespaceName.fromString(newNamespace.getName());
    namespaceDao.insert(newNamespace);
    assertTrue(namespaceDao.exists(found));

    final NamespaceName notFound = NamespaceName.fromString("test");
    assertFalse(namespaceDao.exists(notFound));
  }
}
