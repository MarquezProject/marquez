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
