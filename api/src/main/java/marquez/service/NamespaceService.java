/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import io.prometheus.client.Counter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.BaseDao;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;

@Slf4j
public class NamespaceService extends DelegatingDaos.DelegatingNamespaceDao {
  private static final Counter namespaces =
      Counter.build()
          .namespace("marquez")
          .name("namespace_total")
          .help("Namespace creation invocations")
          .register();

  public NamespaceService(@NonNull final BaseDao baseDao) {
    super(baseDao.createNamespaceDao());
    init();
  }

  private void init() {
    final NamespaceMeta meta =
        new NamespaceMeta(
            OwnerName.ANONYMOUS,
            "The default global namespace for dataset, job, and run metadata "
                + "not belonging to a user-specified namespace.");
    upsertNamespaceMeta(NamespaceName.DEFAULT, meta);
  }

  public Namespace createOrUpdate(@NonNull NamespaceName name, @NonNull NamespaceMeta meta) {
    namespaces.inc();
    return upsertNamespaceMeta(name, meta);
  }
}
