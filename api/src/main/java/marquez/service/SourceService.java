/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import io.prometheus.client.Counter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.SourceName;
import marquez.db.BaseDao;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;

@Slf4j
public class SourceService extends DelegatingDaos.DelegatingSourceDao {
  private static final Counter sources =
      Counter.build()
          .namespace("marquez")
          .name("source_total")
          .help("Number of create source invocations.")
          .register();

  public SourceService(final BaseDao dao) {
    super(dao.createSourceDao());
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Source createOrUpdate(@NonNull SourceName name, @NonNull SourceMeta meta) {
    log.info("Create/upsert source '{}' with meta: {}", name.getValue(), meta);
    sources.inc();
    return upsert(name, meta);
  }
}
