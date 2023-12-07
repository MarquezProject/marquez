/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.Namespace;
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

  @Test
  void testFindAllFilter() {
    var namespaceName1 = NamespaceName.of("postgres://localhost:5432");
    var namespaceMeta1 = new NamespaceMeta(new OwnerName("marquez"), null);
    namespaceDao.upsertNamespaceMeta(namespaceName1, namespaceMeta1);

    var namespaceName2 = NamespaceName.of("s3://bucket");
    var namespaceMeta2 = new NamespaceMeta(new OwnerName("yannick"), null);
    namespaceDao.upsertNamespaceMeta(namespaceName2, namespaceMeta2);

    List<Namespace> namespaces = namespaceDao.findAllFilter("s3.*", 10, 0);

    assertEquals(1, namespaces.size());
    assertEquals(namespaceName1, namespaces.get(0).getName());
  }
}
