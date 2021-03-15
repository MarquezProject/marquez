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

  public Source createOrUpdate(@NonNull SourceName name, @NonNull SourceMeta meta) {
    log.info("Create/upsert source '{}' with meta: {}", name.getValue(), meta);
    sources.inc();
    return upsert(name, meta);
  }
}
