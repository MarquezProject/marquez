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

import static org.junit.jupiter.api.Assertions.assertTrue;

import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.NamespaceMeta;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class NamespaceDaoTest {

  private static NamespaceDao namespaceDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
  }

  @Test
  void testWriteAndReadNamespace() {
    var namespaceName = NamespaceName.of("postgres://localhost:5432");
    var namespaceMeta = new NamespaceMeta(new OwnerName("marquez"), null);
    namespaceDao.upsertNamespaceMeta(namespaceName, namespaceMeta);

    assertTrue(namespaceDao.exists(namespaceName.getValue()));
  }
}
